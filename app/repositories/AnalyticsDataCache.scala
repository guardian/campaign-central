package repositories

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import play.api.Logger
import play.api.libs.json.{Format, Json}
import services.Dynamo
import util.Compression

import scala.util.control.NonFatal
import scala.collection.JavaConversions._

sealed trait CacheResult[+A] extends Product {

  def isEmpty: Boolean

  def get: A

  def getOrElse[B >: A](default: => B): B =
    if (isEmpty) default else this.get
}

case class Hit[+A](x: A) extends CacheResult[A] {
  def isEmpty = false
  def get = x
}

case class Stale[+A](x: A) extends CacheResult[A] {
  def isEmpty = false
  def get = x
}

case object Miss extends CacheResult[Nothing] {
  def isEmpty = true
  def get = throw new NoSuchElementException("Miss.get")
}

case class AnalyticsDataCacheEntrySummary(key: String, dataType: String, expires: Option[Long], written: Long)

object AnalyticsDataCacheEntrySummary {
  implicit val analyticsDataCacheEntrySummaryFormat: Format[AnalyticsDataCacheEntrySummary] = Jsonx.formatCaseClass[AnalyticsDataCacheEntrySummary]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[AnalyticsDataCacheEntrySummary]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to parse analytics data cache item summary ${item.toJSON}", e)
      throw e
    }
  }
}

case class AnalyticsDataCacheEntry(key: String, dataType: String, data: String, expires: Option[Long], written: Long) {
  //def toItem = Item.fromJSON(Json.toJson(this).toString())

  def toItem = {
    val item = new Item()
      .withString("key", key)
      .withString("dataType", dataType)
      .withBinary("compressedData", Compression.compress(data))
      .withLong("written", written)

    expires.foreach(item.withLong("expires", _))

    item
  }

}

object AnalyticsDataCacheEntry {
  implicit val analyticsDataCacheEntryFormat: Format[AnalyticsDataCacheEntry] = Jsonx.formatCaseClass[AnalyticsDataCacheEntry]

  def fromItem(item: Item) = try {
    if (item.isPresent("compressedData")) {
      AnalyticsDataCacheEntry(
        key = item.getString("key"),
        dataType = item.getString("dataType"),
        data = Compression.decompress(item.getBinary("compressedData")),
        expires = if(item.isPresent("expires")) Some(item.getLong("expires")) else None,
        written = item.getLong("written")
      )
    } else {
      Json.parse(item.toJSON).as[AnalyticsDataCacheEntry]
    }
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to parse analytics data cache item ${item.toJSON}", e)
      throw e
    }
  }
}

object AnalyticsDataCache {

  def deleteCacheEntry(key: String, dataType: String): Unit = {
    Dynamo.analyticsDataCacheTable.deleteItem("key", key, "dataType", dataType)
  }

  def putCampaignDailyCountsReport(campaignId: String, data: CampaignDailyCountsReport, validToTimestamp: Option[Long]): Unit = {

    val entry = AnalyticsDataCacheEntry(campaignId, "CampaignDailyCountsReport", Json.toJson(data).toString(), validToTimestamp, System.currentTimeMillis())
    Dynamo.analyticsDataCacheTable.putItem(entry.toItem)
  }

  def putCampaignSummary(campaignId: String, data: CampaignSummary, validToTimestamp: Option[Long]): Unit = {
    val entry = AnalyticsDataCacheEntry(campaignId, "CampaignSummary", Json.toJson(data).toString(), validToTimestamp, System.currentTimeMillis())
    Dynamo.analyticsDataCacheTable.putItem(entry.toItem)
  }

  def putOverallSummary(data: Map[String, CampaignSummary]): Unit = {
    val entry = AnalyticsDataCacheEntry("overall", "CampaignSummary", Json.toJson(data).toString(), None, System.currentTimeMillis())
    Dynamo.analyticsDataCacheTable.putItem(entry.toItem)
  }

  def getCampaignDailyCountsReport(campaignId: String): CacheResult[CampaignDailyCountsReport] = {
    val item = Option(Dynamo.analyticsDataCacheTable.getItem("key", campaignId, "dataType", "CampaignDailyCountsReport"))
    item.map{ i =>
      val entry = AnalyticsDataCacheEntry.fromItem(i)
      val report = Json.parse(entry.data).as[CampaignDailyCountsReport]

      entry.expires match {
        case Some(ts) if ts < System.currentTimeMillis() => Stale(report)
        case _ => Hit(report)
      }
    }.getOrElse(Miss)
  }

  def getCampaignSummary(campaignId: String): CacheResult[CampaignSummary] = {
    val item = Option(Dynamo.analyticsDataCacheTable.getItem("key", campaignId, "dataType", "CampaignSummary"))
    item.map{ i =>
      val entry = AnalyticsDataCacheEntry.fromItem(i)
      val report = Json.parse(entry.data).as[CampaignSummary]

      entry.expires match {
        case Some(ts) if ts < System.currentTimeMillis() => Stale(report)
        case _ => Hit(report)
      }
    }.getOrElse(Miss)
  }

  def getOverallSummary(): CacheResult[Map[String, CampaignSummary]] = {
    val item = Option(Dynamo.analyticsDataCacheTable.getItem("key", "overall", "dataType", "CampaignSummary"))
    item.map{ i =>
      val entry = AnalyticsDataCacheEntry.fromItem(i)
      val report = Json.parse(entry.data).as[Map[String, CampaignSummary]]

      entry.expires match {
        case Some(ts) if ts < System.currentTimeMillis() => Stale(report)
        case _ => Hit(report)
      }
    }.getOrElse(Miss)

  }

  def summariseContents = {
    Dynamo.analyticsDataCacheTable.scan(
      new ScanSpec().withAttributesToGet("key", "dataType", "expires", "written")
    ).map( AnalyticsDataCacheEntrySummary.fromItem ).toList.sortBy(_.key)
  }
}
