package model

import java.time.LocalDate

import com.google.api.ads.dfp.axis.v201608.{DateTime, LineItem}
import play.api.Logger
import play.api.libs.json.{Json, Writes}
import repositories._
import services.Config.conf._
import services.Dfp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.BufferedSource

case class PerformanceStats(impressions: Int, clicks: Int) {
  val ctr: Double = if (impressions == 0) 0 else clicks.toDouble / impressions * 100
}

object PerformanceStats {

  implicit val jsonReads = Json.reads[PerformanceStats]

  implicit val jsonWrites = new Writes[PerformanceStats] {
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
    val groups = for {
      campaign <- CampaignRepository.getCampaign(campaignId)
      nativeCardOrderId <- dfpNativeCardOrderIds.get(campaign.`type`)
      merchandisingOrderId <- dfpMerchandisingOrderIds.get(campaign.`type`)
    } yield {
      val dfpSession = Dfp.mkSession()
      def trafficDrivers(orderIds: Seq[Long]): Seq[TrafficDriver] =
        Dfp.fetchLineItemsByOrder(dfpSession, orderIds) filter {
          Dfp.hasCampaignIdCustomFieldValue(campaignId)
        } map TrafficDriver.fromDfpLineItem
      Seq(
        TrafficDriverGroup.fromTrafficDrivers("Native cards", trafficDrivers(nativeCardOrderId)),
        TrafficDriverGroup.fromTrafficDrivers("Merchandising", trafficDrivers(merchandisingOrderId))
      ).flatten
    }
    groups getOrElse Nil
  }
}

case class DayStats(date: LocalDate, stats: PerformanceStats)

object DayStats {

  implicit val jsonFormat = Json.format[DayStats]

  def fromDfpReport(report: BufferedSource): Seq[DayStats] = {
    report.getLines.toSeq.tail.map { line =>
      val parts = line.split(",")
      DayStats(LocalDate.parse(parts(0)), PerformanceStats(parts(1).toInt, parts(2).toInt))
    }
  }
}

case class TrafficDriverGroupStats(groupName: String, dayStats: Seq[DayStats])

object TrafficDriverGroupStats {

  implicit val jsonFormat = Json.format[TrafficDriverGroupStats]

  def forCampaign(campaignId: String): Seq[TrafficDriverGroupStats] = {

    AnalyticsDataCache.getCampaignTrafficDriverGroupStats(campaignId) match {

      case Hit(report) =>
        Logger.debug(s"getting traffic driver stats for campaign $campaignId - cache hit")
        report

      case Stale(report) =>
        Logger.debug(s"getting traffic driver stats for campaign $campaignId - cache stale spawning async refresh")
        Future {
          Logger.debug(s"async refresh of traffic driver stats for campaign $campaignId")
          fetchAndStoreStats(campaignId)
        }
        report

      case Miss =>
        Logger.debug(s"getting traffic driver stats for campaign $campaignId - cache miss fetching sync")
        fetchAndStoreStats(campaignId)
    }
  }

  private def fetchAndStoreStats(campaignId: String): Seq[TrafficDriverGroupStats] = {
    val stats = for {
      campaign <- CampaignRepository.getCampaign(campaignId)
      nativeCardOrderIds <- dfpNativeCardOrderIds.get(campaign.`type`)
      merchandisingOrderIds <- dfpMerchandisingOrderIds.get(campaign.`type`)
    } yield {
      val dfpSession = Dfp.mkSession()
      def fetchStats(groupName: String, orderIds: Seq[Long]): TrafficDriverGroupStats = {
        val lineItemIds = Dfp.fetchLineItemsByOrder(dfpSession, orderIds) filter {
          Dfp.hasCampaignIdCustomFieldValue(campaignId)
        } map (_.getId.toLong)
        TrafficDriverGroupStats(
          groupName,
          Dfp.fetchStatsReport(dfpSession, lineItemIds).map(DayStats.fromDfpReport) getOrElse Nil
        )
      }
      val fetched = Seq(
        ("Native cards", nativeCardOrderIds),
        ("Merchandising", merchandisingOrderIds)
      ).par.map { case (groupName, orderIds) =>
        fetchStats(groupName, orderIds)
      }.toList
      AnalyticsDataCache.putCampaignTrafficDriverGroupStats(campaignId, fetched)
      fetched
    }
    stats getOrElse Nil
  }
}

case class LineItemSummary(id: Long, name: String)

object LineItemSummary {

  implicit val writes = Json.writes[LineItemSummary]

  def fromLineItem(item: LineItem) = LineItemSummary(
    id = item.getId,
    name = item.getName
  )

  def suggestedTrafficDriversForCampaign(campaignId: String): Map[String, Seq[LineItemSummary]] = {
    val lineItems = for {
      campaign <- CampaignRepository.getCampaign(campaignId)
      client <- ClientRepository.getClient(campaign.clientId)
      nativeCardOrderIds <- dfpNativeCardOrderIds.get(campaign.`type`)
      merchandisingCardOrderIds <- dfpMerchandisingOrderIds.get(campaign.`type`)
    } yield {
      def fetch(orderIds: Seq[Long]) =
        Dfp.fetchSuggestedLineItems(campaign.name, client.name, Dfp.mkSession(), orderIds) map {
          LineItemSummary.fromLineItem
      }
      Map(
        "Native cards" -> fetch(nativeCardOrderIds),
        "Merchandising" -> fetch(merchandisingCardOrderIds)
      )
    }
    lineItems getOrElse Map.empty
  }
}
