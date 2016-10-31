package repositories

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json.{Format, Json}
import services.Dynamo

import scala.util.control.NonFatal

abstract sealed trait CacheResult[+A] extends Product

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

case class AnalyticsDataCacheEntry(key: String, dataType: String, data: String, expires: Option[Long], written: Long) {
  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object AnalyticsDataCacheEntry {
  implicit val analyticsDataCacheEntryFormat: Format[AnalyticsDataCacheEntry] = Jsonx.formatCaseClass[AnalyticsDataCacheEntry]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[AnalyticsDataCacheEntry]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to parse analytics data cache item ${item.toJSON}", e)
      throw e
    }
  }
}

object AnalyticsDataCache {

  def putCampaignDailyCountsReport(campaignId: String, data: CampaignDailyCountsReport, expires: Option[Long]): Unit = {
    val entry = AnalyticsDataCacheEntry(campaignId, "CampaignDailyCountsReport", Json.toJson(data).toString(), expires, System.currentTimeMillis())
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
}
