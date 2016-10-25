package model

import java.time.LocalDate

import com.google.api.ads.dfp.axis.v201608.{DateTime, LineItem}
import play.api.libs.json.{Json, Writes}
import services.Config.conf._
import services.{DfpFetcher, DfpFilter}
import util.AnalyticsCache

import scala.io.BufferedSource

case class PerformanceStats(impressions: Int, clicks: Int) {
  val ctr: Double = if (impressions == 0) 0 else clicks.toDouble / impressions * 100
}

object PerformanceStats {

  implicit val writes = new Writes[PerformanceStats] {
    def writes(stats: PerformanceStats) = Json.obj(
      "impressions" -> stats.impressions,
      "clicks" -> stats.clicks,
      "ctr" -> stats.ctr
    )
  }

  def sum(stats: Seq[PerformanceStats]) = PerformanceStats(
    impressions = stats.map(_.impressions).sum,
    clicks = stats.map(_.clicks).sum
  )
}

case class TrafficDriver(
  id: Long,
  name: String,
  url: String,
  status: String,
  startDate: LocalDate,
  endDate: LocalDate,
  summaryStats: PerformanceStats
)

object TrafficDriver {

  def fromDfpLineItem(lineItem: LineItem): TrafficDriver = {

    def mkLocalDate(dfpDateTime: DateTime): LocalDate = {
      val date = dfpDateTime.getDate
      LocalDate.of(date.getYear, date.getMonth, date.getDay)
    }

    TrafficDriver(
      id = lineItem.getId,
      name = lineItem.getName,
      url = s"https://www.google.com/dfp/$dfpNetworkCode#delivery/LineItemDetail/lineItemId=${lineItem.getId}",
      status = lineItem.getStatus.getValue,
      startDate = mkLocalDate(lineItem.getStartDateTime),
      endDate = mkLocalDate(lineItem.getEndDateTime),
      summaryStats = PerformanceStats(
        impressions = Option(lineItem.getStats) map (_.getImpressionsDelivered.toInt) getOrElse 0,
        clicks = Option(lineItem.getStats) map (_.getClicksDelivered.toInt) getOrElse 0
      )
    )
  }
}

case class TrafficDriverGroup(
  groupName: String,
  startDate: LocalDate,
  endDate: LocalDate,
  summaryStats: PerformanceStats,
  trafficDriverUrls: Seq[String]
)

object TrafficDriverGroup {

  implicit val writes = Json.writes[TrafficDriverGroup]

  implicit object DateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  def fromTrafficDrivers(groupName: String, trafficDrivers: Seq[TrafficDriver]): Option[TrafficDriverGroup] = {
    if (trafficDrivers.isEmpty) None
    else Some(
      TrafficDriverGroup(
        groupName,
        startDate = trafficDrivers.map(_.startDate).min,
        endDate = trafficDrivers.map(_.endDate).max,
        summaryStats = PerformanceStats.sum(trafficDrivers.map(_.summaryStats)),
        trafficDriverUrls = trafficDrivers.map(_.url)
      )
    )
  }

  def forCampaign(campaignId: String): Seq[TrafficDriverGroup] = {
    val dfpSession = DfpFetcher.mkSession()

    def trafficDrivers(orderId: Long): Seq[TrafficDriver] =
      DfpFetcher.fetchLineItemsByOrder(dfpSession, orderId) filter {
        DfpFilter.hasCampaignIdCustomFieldValue(campaignId)
      } map TrafficDriver.fromDfpLineItem

    Seq(
      TrafficDriverGroup.fromTrafficDrivers("Native cards", trafficDrivers(dfpNativeCardOrderId)),
      TrafficDriverGroup.fromTrafficDrivers("Merchandising", trafficDrivers(dfpMerchandisingOrderId))
    ).flatten
  }
}

case class DayStats(date: LocalDate, stats: PerformanceStats)

object DayStats {

  implicit val writes = Json.writes[DayStats]

  def fromDfpReport(report: BufferedSource): Seq[DayStats] = {
    report.getLines.toSeq.tail.map { line =>
      val parts = line.split(",")
      DayStats(LocalDate.parse(parts(0)), PerformanceStats(parts(1).toInt, parts(2).toInt))
    }
  }
}

case class TrafficDriverGroupStats(groupName: String, dayStats: Seq[DayStats])

object TrafficDriverGroupStats {

  implicit val writes = Json.writes[TrafficDriverGroupStats]

  private val statsCache = new AnalyticsCache[String, Seq[TrafficDriverGroupStats]]

  def forCampaign(campaignId: String): Seq[TrafficDriverGroupStats] = {
    val cachedStats = statsCache.get(campaignId) getOrElse Nil
    if (cachedStats.isEmpty) {
      loadStatsForCampaign(campaignId)
    }
    cachedStats
  }

  private def loadStatsForCampaign(campaignId: String): Unit = {

    val dfpSession = DfpFetcher.mkSession()

    def groupStats(groupName: String, orderId: Long): TrafficDriverGroupStats = {

      val lineItemIds = DfpFetcher.fetchLineItemsByOrder(dfpSession, orderId) filter {
        DfpFilter.hasCampaignIdCustomFieldValue(campaignId)
      } map (_.getId.toLong)

      TrafficDriverGroupStats(
        groupName,
        DfpFetcher.fetchStatsReport(dfpSession, lineItemIds).map(DayStats.fromDfpReport) getOrElse Nil
      )
    }

    val stats = Seq(
      ("Native cards", dfpNativeCardOrderId),
      ("Merchandising", dfpMerchandisingOrderId)
    ).par.map { case (groupName, orderId) =>
      groupStats(groupName, orderId)
    }.toList

    statsCache.put(campaignId, stats)
  }
}
