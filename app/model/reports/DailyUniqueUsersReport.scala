package model.reports

import ai.x.play.json.Jsonx
import model.Campaign
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Format
import repositories._

import scala.concurrent.Future

case class DailyUniqueUserEntry(date: DateTime, uniqueUsers: Long, cumulativeUniqueUsers: Long)

object DailyUniqueUserEntry{
  implicit val dailyUniqueUserEntryFormat: Format[DailyUniqueUserEntry] = Jsonx.formatCaseClass[DailyUniqueUserEntry]
}

case class DailyUniqueUsersReport(campaignId: String, dailyUniqueUsers: List[DailyUniqueUserEntry]) {
  def refresh = {
    val refreshed = for (
      campaign <- CampaignRepository.getCampaign(campaignId);
      lastData <- dailyUniqueUsers.lastOption
    ) {
      val missingDays = DateBasedReport.calculateDatesToFetch(lastData.date.plusDays(1), DateTime.now)
      val dailyReports = missingDays.map(DailyUniqueUsersReport.loadCampaignDailyUniquesForDay(campaign, _))

      val refreshed = dailyReports.foldLeft(this) { case (report: DailyUniqueUsersReport, (date: DateTime, dailyUniqueUsers: Long)) =>
        report.addDayCounts(date, dailyUniqueUsers)
      }
      AnalyticsDataCache.putDailyUniqueUsersReport(campaignId, refreshed, AnalyticsDataCache.calculateValidToDateForDailyStats(campaign))

      refreshed
    }

  }

  def addDayCounts(date: DateTime, dailyUniqueCount: Long): DailyUniqueUsersReport = {

    dailyUniqueUsers.lastOption match{
      case Some(latest) => {
        val newData = DailyUniqueUserEntry(date, dailyUniqueCount, latest.cumulativeUniqueUsers + dailyUniqueCount)
        DailyUniqueUsersReport(campaignId, dailyUniqueUsers :+ newData)
      }
      case None => {
        val newData = DailyUniqueUserEntry(date, dailyUniqueCount, dailyUniqueCount)
        DailyUniqueUsersReport(campaignId, List(newData))
      }
    }
  }
}

object DailyUniqueUsersReport {

  implicit val ec = AnalyticsDataCache.analyticsExectuionContext

  implicit val dailyUniqueUsersReportFormat: Format[DailyUniqueUsersReport] = Jsonx.formatCaseClass[DailyUniqueUsersReport]

  def getDailyUniqueUsersReport(campaignId: String): Option[DailyUniqueUsersReport] = {

    AnalyticsDataCache.getDailyUniqueUsersReport(campaignId) match {
      case Hit(report) => {
        Logger.debug(s"getting daily unique users report for campaign $campaignId - cache hit")
        Some(report)
      }
      case Stale(report) => {
        Logger.debug(s"getting daily unique users report for campaign $campaignId - cache stale spawning async refresh")

        Future{
          Logger.debug(s"async refresh of daily unique users report for campaign $campaignId")
          report.refresh
        } // serve stale but spawn refresh future
        Some(report)
      }
      case Miss => {
        Logger.debug(s"getting daily unique users report for campaign $campaignId - cache miss fetching sync")

        generateReport(campaignId)
      }
    }
  }

  def generateReport(campaignId: String): Option[DailyUniqueUsersReport] = {
    for (
      campaign <- CampaignRepository.getCampaign(campaignId);
      startDate <- campaign.startDate
    ) yield {
      val dailyReports = DateBasedReport.calculateDatesToFetch(startDate, DateTime.now).map{ dt =>
        Thread.sleep(1000) // try to avoid rate limiting
        loadCampaignDailyUniquesForDay(campaign, dt)
      }

      val emptyReport = DailyUniqueUsersReport(campaignId, Nil)
      val report = dailyReports.foldLeft(emptyReport) { case (report: DailyUniqueUsersReport, (date: DateTime, dailyUniqueCount: Long)) =>
        report.addDayCounts(date, dailyUniqueCount)
      }

      AnalyticsDataCache.putDailyUniqueUsersReport(campaignId, report, AnalyticsDataCache.calculateValidToDateForDailyStats(campaign))

      report
    }
  }

  def loadCampaignDailyUniquesForDay(campaign: Campaign, date: DateTime) = {
    val dailyCount = if (campaign.`type` == "hosted") {
      campaign.gaFilterExpression.map(GoogleAnalytics.loadUniqueUsersDay(_, date))
    } else {
      campaign.pathPrefix.map{ section => GoogleAnalytics.loadUniqueUsersDay(s"ga:dimension4==${section}", date)}
    }

    date -> dailyCount.getOrElse(0L)
  }

}
