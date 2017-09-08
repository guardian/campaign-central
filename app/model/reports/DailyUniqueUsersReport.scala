package model.reports

import ai.x.play.json.Jsonx
import model.Campaign
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Format
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._
import repositories._

import scala.concurrent.{ExecutionContextExecutor, Future}

case class DailyUniqueUserEntry(date: DateTime, uniqueUsers: Long, cumulativeUniqueUsers: Long)

object DailyUniqueUserEntry {
  implicit val dailyUniqueUserEntryFormat: Format[DailyUniqueUserEntry] = Jsonx.formatCaseClass[DailyUniqueUserEntry]
}

case class DailyUniqueUsersReport(campaignId: String, dailyUniqueUsers: List[DailyUniqueUserEntry]) {
  def refresh(
    campaignRepository: CampaignRepository,
    analyticsDataCache: AnalyticsDataCache,
    googleAnalytics: GoogleAnalytics
  ): Unit = {
    val refreshed: Unit = for (campaign <- campaignRepository.getCampaign(campaignId);
                               lastData <- dailyUniqueUsers.lastOption) {
      val missingDays = DateBasedReport.calculateDatesToFetch(lastData.date.plusDays(1), campaign.endDate)
      val dailyReports =
        missingDays.map(DailyUniqueUsersReport.loadCampaignDailyUniquesForDay(googleAnalytics, campaign, _))

      val refreshed = dailyReports.foldLeft(this) {
        case (report: DailyUniqueUsersReport, (date: DateTime, dailyUniqueUsers: Long)) =>
          report.addDayCounts(date, dailyUniqueUsers)
      }
      analyticsDataCache.putDailyUniqueUsersReport(
        campaignId,
        refreshed,
        analyticsDataCache.calculateValidToDateForDailyStats(campaign)
      )

      CampaignSummary.storeLatestUniquesForCampaign(analyticsDataCache, campaign, refreshed.dailyUniqueUsers.lastOption)

      refreshed
    }

  }

  def addDayCounts(date: DateTime, dailyUniqueCount: Long): DailyUniqueUsersReport = {

    dailyUniqueUsers.lastOption match {
      case Some(latest) =>
        val newData = DailyUniqueUserEntry(date, dailyUniqueCount, latest.cumulativeUniqueUsers + dailyUniqueCount)
        DailyUniqueUsersReport(campaignId, dailyUniqueUsers :+ newData)
      case None =>
        val newData = DailyUniqueUserEntry(date, dailyUniqueCount, dailyUniqueCount)
        DailyUniqueUsersReport(campaignId, List(newData))
    }
  }
}

object DailyUniqueUsersReport {

  implicit val dailyUniqueUsersReportFormat: Format[DailyUniqueUsersReport] =
    Jsonx.formatCaseClass[DailyUniqueUsersReport]

  def getDailyUniqueUsersReport(
    campaignRepository: CampaignRepository,
    analyticsDataCache: AnalyticsDataCache,
    googleAnalytics: GoogleAnalytics,
    campaignId: String
  ): Option[DailyUniqueUsersReport] = {
    implicit val ec: ExecutionContextExecutor = analyticsDataCache.analyticsExecutionContext

    analyticsDataCache.getDailyUniqueUsersReport(campaignId) match {
      case Hit(report) =>
        Logger.debug(s"getting daily unique users report for campaign $campaignId - cache hit")
        Some(report)
      case Stale(report) =>
        Logger.debug(s"getting daily unique users report for campaign $campaignId - cache stale spawning async refresh")

        Future {
          Logger.debug(s"async refresh of daily unique users report for campaign $campaignId")
          report.refresh(campaignRepository, analyticsDataCache, googleAnalytics)
        } // serve stale but spawn refresh future
        Some(report)
      case Miss =>
        Logger.debug(s"getting daily unique users report for campaign $campaignId - cache miss fetching sync")

        generateReport(campaignRepository, analyticsDataCache, googleAnalytics, campaignId)
    }
  }

  def generateReport(campaignRepository: CampaignRepository,
                     analyticsDataCache: AnalyticsDataCache,
                     googleAnalytics: GoogleAnalytics,
                     campaignId: String): Option[DailyUniqueUsersReport] = {
    for {
      campaign  <- campaignRepository.getCampaign(campaignId)
      startDate <- campaign.startDate
    } yield {
      val dailyReports = DateBasedReport.calculateDatesToFetch(startDate, campaign.endDate).map { dt =>
        Thread.sleep(3000) // try to avoid rate limiting
        loadCampaignDailyUniquesForDay(googleAnalytics, campaign, dt)
      }

      val emptyReport = DailyUniqueUsersReport(campaignId, Nil)
      val report = dailyReports.foldLeft(emptyReport) {
        case (report: DailyUniqueUsersReport, (date: DateTime, dailyUniqueCount: Long)) =>
          report.addDayCounts(date, dailyUniqueCount)
      }

      analyticsDataCache.putDailyUniqueUsersReport(campaignId,
                                                   report,
                                                   analyticsDataCache.calculateValidToDateForDailyStats(campaign))
      CampaignSummary.storeLatestUniquesForCampaign(analyticsDataCache, campaign, report.dailyUniqueUsers.lastOption)

      report
    }
  }

  def loadCampaignDailyUniquesForDay(googleAnalytics: GoogleAnalytics,
                                     campaign: Campaign,
                                     date: DateTime): (DateTime, Long) = {
    val dailyCount = if (campaign.`type` == "hosted") {
      campaign.gaFilterExpression.map(googleAnalytics.loadUniqueUsersDay(_, date))
    } else {
      campaign.pathPrefix.map { section =>
        googleAnalytics.loadUniqueUsersDay(s"ga:dimension4==$section", date)
      }
    }

    date -> dailyCount.getOrElse(0L)
  }

}
