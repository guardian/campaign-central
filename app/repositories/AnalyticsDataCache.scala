package repositories

import java.util.concurrent.Executors

import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import model.reports.{AnalyticsDataCacheEntry, AnalyticsDataCacheEntrySummary, CtaClicksReport}
import model.{Campaign, CampaignDailyCountsReport, CampaignSummary, TrafficDriverGroupStats}
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}
import services.Dynamo

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

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


object AnalyticsDataCache {

  implicit val analyticsExectuionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(50))

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

  def putCampaignCtaClicksReport(campaignId: String, data: CtaClicksReport, validToTimestamp: Option[Long]): Unit = {
    val entry = AnalyticsDataCacheEntry(campaignId, "CtaClicksReport", Json.toJson(data).toString(), validToTimestamp, System.currentTimeMillis())
    Dynamo.analyticsDataCacheTable.putItem(entry.toItem)
  }

  def putCampaignTrafficDriverGroupStats( campaignId: String, data: Seq[TrafficDriverGroupStats]): Unit = {
    val entry = AnalyticsDataCacheEntry(
      key = campaignId,
      dataType = "TrafficDriverGroupStats",
      data = Json.toJson(data).toString(),
      expires = Some(DateTime.now().withTimeAtStartOfDay().plusDays(1).plusSeconds(1).getMillis),
      written = System.currentTimeMillis()
    )
    Dynamo.analyticsDataCacheTable.putItem(entry.toItem)
  }

  private def getEntry[T](key: String, dataType: String)(implicit fjs: Reads[T]): CacheResult[T] = {
    val item = Option(Dynamo.analyticsDataCacheTable.getItem("key", key, "dataType", dataType))
    item.map{ i =>
      val entry = AnalyticsDataCacheEntry.fromItem(i)
      val report = Json.parse(entry.data).as[T]

      entry.expires match {
        case Some(ts) if ts < System.currentTimeMillis() => Stale(report)
        case _ => Hit(report)
      }
    }.getOrElse(Miss)
  }

  def getCampaignDailyCountsReport(campaignId: String): CacheResult[CampaignDailyCountsReport] = {
    getEntry[CampaignDailyCountsReport](campaignId, "CampaignDailyCountsReport")
  }

  def getCampaignSummary(campaignId: String): CacheResult[CampaignSummary] = {
    getEntry[CampaignSummary](campaignId, "CampaignSummary")
  }

  def getOverallSummary(): CacheResult[Map[String, CampaignSummary]] = {
    getEntry[Map[String, CampaignSummary]]("overall", "CampaignSummary")

  }

  def getCampaignCtaClicksReport(campaignId: String): CacheResult[CtaClicksReport] = {
    getEntry[CtaClicksReport](campaignId, "CtaClicksReport")
  }

  def summariseContents = {
    Dynamo.analyticsDataCacheTable.scan(
      new ScanSpec().withAttributesToGet("key", "dataType", "expires", "written")
    ).map( AnalyticsDataCacheEntrySummary.fromItem ).toList.sortBy(_.key)
  }

  def getCampaignTrafficDriverGroupStats( campaignId: String): CacheResult[Seq[TrafficDriverGroupStats]] =
    getEntry[Seq[TrafficDriverGroupStats]](campaignId, "TrafficDriverGroupStats")

  def calculateValidToDateForDailyStats(campaign: Campaign): Option[Long] = {
    val campaignFinished = for (
      d <- campaign.endDate
    ) yield {d.isBeforeNow}

    if(campaignFinished.getOrElse(false)) None else { Some( DateTime.now().withTimeAtStartOfDay().plusDays(1).getMillis) }
  }
}
