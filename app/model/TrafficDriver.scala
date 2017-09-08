package model

import java.time.LocalDate

import com.google.api.ads.dfp.axis.v201705.{DateTime, LineItem}
import play.api.Logger
import play.api.libs.json._
import repositories._
import services.{Config, Dfp}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.BufferedSource

case class PerformanceStats(impressions: Int, clicks: Int) {
  val ctr: Double = if (impressions == 0) 0 else clicks.toDouble / impressions * 100
}

object PerformanceStats {

  implicit val jsonReads: Reads[PerformanceStats] = Json.reads[PerformanceStats]

  implicit val jsonWrites: Writes[PerformanceStats] = new Writes[PerformanceStats] {
    def writes(stats: PerformanceStats): JsObject = Json.obj(
      "impressions" -> stats.impressions,
      "clicks"      -> stats.clicks,
      "ctr"         -> stats.ctr
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
  endDate: Option[LocalDate],
  summaryStats: PerformanceStats
)

object TrafficDriver {

  def fromDfpLineItem(config: Config)(lineItem: LineItem): TrafficDriver = {

    def mkLocalDate(dfpDateTime: DateTime): Option[LocalDate] = {
      Option(dfpDateTime) map { dateTime =>
        val date = dateTime.getDate
        LocalDate.of(date.getYear, date.getMonth, date.getDay)
      }
    }

    TrafficDriver(
      id = lineItem.getId,
      name = lineItem.getName,
      url = LineItemUrl(config, lineItem.getId),
      status = lineItem.getStatus.getValue,
      startDate = mkLocalDate(lineItem.getStartDateTime).getOrElse(LocalDate.now),
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

  implicit val writes: Writes[TrafficDriverGroup] = Json.writes[TrafficDriverGroup]

  implicit object DateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  def fromTrafficDrivers(groupName: String, trafficDrivers: Seq[TrafficDriver]): Option[TrafficDriverGroup] = {
    if (trafficDrivers.isEmpty) None
    else
      Some(
        TrafficDriverGroup(
          groupName,
          startDate = trafficDrivers.map(_.startDate).min,
          endDate = trafficDrivers.map(_.endDate.getOrElse(LocalDate.MIN)).max,
          summaryStats = PerformanceStats.sum(trafficDrivers.map(_.summaryStats)),
          trafficDriverUrls = trafficDrivers.map(_.url)
        )
      )
  }

  def forCampaign(config: Config,
                  campaignRepository: CampaignRepository,
                  campaignId: String): Seq[TrafficDriverGroup] = {
    val groups = for {
      campaign             <- campaignRepository.getCampaign(campaignId)
      nativeCardOrderId    <- config.dfpNativeCardOrderIds.get(campaign.`type`)
      merchandisingOrderId <- config.dfpMerchandisingOrderIds.get(campaign.`type`)
    } yield {
      val lineItemService = Dfp.mkLineItemService(Dfp.mkSession(config))
      def trafficDrivers(orderIds: Seq[Long]): Seq[TrafficDriver] =
        Dfp.fetchLineItemsByOrder(lineItemService, orderIds) filter {
          Dfp.hasCampaignIdCustomFieldValue(config, campaignId)
        } map TrafficDriver.fromDfpLineItem(config)
      Seq(
        fromTrafficDrivers("Native cards", trafficDrivers(nativeCardOrderId)),
        fromTrafficDrivers("Merchandising", trafficDrivers(merchandisingOrderId))
      ).flatten
    }
    groups getOrElse Nil
  }
}

case class DayStats(date: LocalDate, stats: PerformanceStats)

object DayStats {

  implicit val jsonFormat: Format[DayStats] = Json.format[DayStats]

  def fromDfpReport(report: BufferedSource): Seq[DayStats] = {
    report.getLines.toSeq.drop(1).map { line =>
      val parts = line.split(",")
      DayStats(LocalDate.parse(parts(0)), PerformanceStats(parts(1).toInt, parts(2).toInt))
    }
  }
}

case class TrafficDriverGroupStats(groupName: String, dayStats: Seq[DayStats])

object TrafficDriverGroupStats {

  implicit val jsonFormat: Format[TrafficDriverGroupStats] = Json.format[TrafficDriverGroupStats]

  def forCampaign(
    config: Config,
    campaignRepository: CampaignRepository,
    analyticsDataCache: AnalyticsDataCache,
    campaignId: String
  ): Seq[TrafficDriverGroupStats] = {

    analyticsDataCache.getCampaignTrafficDriverGroupStats(campaignId) match {

      case Hit(report) =>
        Logger.debug(s"getting traffic driver stats for campaign $campaignId - cache hit")
        report

      case Stale(report) =>
        Logger.debug(s"getting traffic driver stats for campaign $campaignId - cache stale spawning async refresh")
        Future {
          Logger.debug(s"async refresh of traffic driver stats for campaign $campaignId")
          fetchAndStoreStats(config, campaignRepository, analyticsDataCache, campaignId)
        }
        report

      case Miss =>
        Logger.debug(s"getting traffic driver stats for campaign $campaignId - cache miss fetching sync")
        fetchAndStoreStats(config, campaignRepository, analyticsDataCache, campaignId)
    }
  }

  private def fetchAndStoreStats(
    config: Config,
    campaignRepository: CampaignRepository,
    analyticsDataCache: AnalyticsDataCache,
    campaignId: String
  ): Seq[TrafficDriverGroupStats] = {
    val stats = for {
      campaign              <- campaignRepository.getCampaign(campaignId)
      nativeCardOrderIds    <- config.dfpNativeCardOrderIds.get(campaign.`type`)
      merchandisingOrderIds <- config.dfpMerchandisingOrderIds.get(campaign.`type`)
    } yield {
      val dfpSession         = Dfp.mkSession(config)
      val dfpLineItemService = Dfp.mkLineItemService(dfpSession)
      val dfpReportService   = Dfp.mkReportService(dfpSession)
      def fetchStats(groupName: String, orderIds: Seq[Long]): TrafficDriverGroupStats = {
        val lineItemIds = Dfp.fetchLineItemsByOrder(dfpLineItemService, orderIds) filter {
          Dfp.hasCampaignIdCustomFieldValue(config, campaignId)
        } map (_.getId.toLong)
        TrafficDriverGroupStats(
          groupName,
          Dfp.fetchStatsReport(dfpReportService, lineItemIds).map(DayStats.fromDfpReport) getOrElse Nil
        )
      }
      val fetched = Seq(
        ("Native cards", nativeCardOrderIds),
        ("Merchandising", merchandisingOrderIds)
      ).par.map {
        case (groupName, orderIds) =>
          fetchStats(groupName, orderIds)
      }.toList
      analyticsDataCache.putCampaignTrafficDriverGroupStats(campaignId, fetched)
      fetched
    }
    stats getOrElse Nil
  }
}

case class LineItemSummary(id: Long, name: String, url: String)

object LineItemSummary {

  implicit val writes: Writes[LineItemSummary] = Json.writes[LineItemSummary]

  def fromLineItem(config: Config)(item: LineItem) = LineItemSummary(
    id = item.getId,
    name = item.getName,
    url = LineItemUrl(config, item.getId)
  )

  def suggestedTrafficDriversForCampaign(
    config: Config,
    campaignRepository: CampaignRepository,
    clientRepository: ClientRepository,
    trafficDriverRejectRepository: TrafficDriverRejectRepository,
    campaignId: String
  ): Map[String, Seq[LineItemSummary]] = {
    val lineItems = for {
      campaign                  <- campaignRepository.getCampaign(campaignId)
      client                    <- clientRepository.getClient(campaign.clientId)
      nativeCardOrderIds        <- config.dfpNativeCardOrderIds.get(campaign.`type`)
      merchandisingCardOrderIds <- config.dfpMerchandisingOrderIds.get(campaign.`type`)
    } yield {
      val dfpLineItemService = Dfp.mkLineItemService(Dfp.mkSession(config))
      def fetch(orderIds: Seq[Long]): Seq[LineItemSummary] =
        Dfp.fetchSuggestedLineItems(
          config = config,
          campaignName = campaign.name,
          clientName = client.name,
          service = dfpLineItemService,
          orderIds,
          lineItemIdsToIgnore = trafficDriverRejectRepository.getRejectedDriverIds(campaignId)
        ) filterNot {
          // DFP won't let you update archived line items
          _.getIsArchived
        } map {
          fromLineItem(config)
        }
      Map(
        "Native cards"  -> fetch(nativeCardOrderIds),
        "Merchandising" -> fetch(merchandisingCardOrderIds)
      ).filterNot { case (_, drivers) => drivers.isEmpty }
    }
    lineItems getOrElse Map.empty
  }

  def acceptSuggestedTrafficDriver(config: Config, campaignId: String, lineItemId: Long): Unit = {
    Dfp.linkLineItemToCampaign(config, Dfp.mkLineItemService(Dfp.mkSession(config)), lineItemId, campaignId)
  }

  def rejectSuggestedTrafficDriver(
    trafficDriverRejectRepository: TrafficDriverRejectRepository,
    campaignId: String,
    lineItemId: Long
  ): Unit = {
    trafficDriverRejectRepository.putRejectedDriverId(campaignId, lineItemId)
  }
}

object LineItemUrl {
  def apply(config: Config, lineItemId: Long) =
    s"https://www.google.com/dfp/${config.dfpNetworkCode}#delivery/LineItemDetail/lineItemId=$lineItemId"
}
