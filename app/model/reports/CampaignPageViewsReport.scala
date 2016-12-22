package model.reports

import ai.x.play.json.Jsonx
import model.Campaign
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Format
import repositories.GoogleAnalytics.DailyViewCounts
import repositories._

import scala.concurrent.Future


case class CampaignPageViewsReport(campaignId: String, seenPaths: Set[String], pageCountStats: List[Map[String, Long]]) {
  def refresh = {
    val refreshed = for (
      campaign <- CampaignRepository.getCampaign(campaignId);
      lastData <- pageCountStats.lastOption;
      lastSeenDate <- lastData.get("date")
    ) {
      val missingDays = DateBasedReport.calculateDatesToFetch(new DateTime(lastSeenDate).plusDays(1), DateTime.now)
      val dailyReports = missingDays.map(CampaignPageViewsReport.loadCampaignPageViewsForDay(campaign, _))

      val refreshed = dailyReports.foldLeft(this) { case (report: CampaignPageViewsReport, (date: DateTime, dailyViewCounts: DailyViewCounts)) =>
        report.addDayCounts(date, dailyViewCounts)
      }
      AnalyticsDataCache.putCampaignPageViewsReport(campaignId, refreshed, AnalyticsDataCache.calculateValidToDateForDailyStats(campaign))

      refreshed
    }

  }

  def addDayCounts(date: DateTime, dailyViewCounts: DailyViewCounts): CampaignPageViewsReport = {

    val newPaths = dailyViewCounts.seenPaths -- seenPaths
    val backfilledCurrentStats = backfillNewPathData(newPaths, pageCountStats)

    val missingPathsInNewData = seenPaths -- dailyViewCounts.seenPaths
    val newDayCounts = addMissingPathsToIncomingData(missingPathsInNewData, dailyViewCounts.countStats)

    val newCountsWithRunningTotals = backfilledCurrentStats.lastOption match{
      case Some(latest) => addRunningTotals(latest, newDayCounts)
      case None => initialiseRunningTotals(newDayCounts)
    }

    val newCountsWithDate = newCountsWithRunningTotals + ("date" -> date.getMillis)

    CampaignPageViewsReport(campaignId, seenPaths ++ dailyViewCounts.seenPaths, pageCountStats :+ newCountsWithDate)
  }

  private def addMissingPathsToIncomingData(missingPathsInNewData: Set[String], dailyViewCounts: Map[String, Long]): Map[String, Long] = {
    val zeroedStatsByPath = missingPathsInNewData map { p =>
      Map(
        s"count$p" -> 0L,
        s"unique$p" -> 0L
      )
    }
    val zeroedStats = zeroedStatsByPath.fold(Map())(_ ++ _)
    dailyViewCounts ++ zeroedStats
  }

  private def backfillNewPathData(newPaths: Set[String], stats: List[Map[String, Long]]) = {
    val backfillZeroedStatsByPath = newPaths map { p =>
      Map(
        s"count$p" -> 0L,
        s"unique$p" -> 0L,
        s"cumulative-count$p" -> 0L,
        s"cumulative-unique$p" -> 0L
      )
    }
    val backfillZeroedStats = backfillZeroedStatsByPath.fold(Map())(_ ++ _)
    stats.map { dayStats =>
      dayStats ++ backfillZeroedStats
    }
  }

  def addRunningTotals(latestCurrentStats: Map[String, Long], newDayCounts: Map[String, Long]): Map[String, Long] = {
    newDayCounts.keys.foldLeft(newDayCounts){case (counts, statName) =>
      val cumalativeStatName = s"cumulative-$statName"
      counts + (cumalativeStatName -> (latestCurrentStats.getOrElse(cumalativeStatName, 0L) + newDayCounts(statName)))
    }
  }

  def initialiseRunningTotals(newDayCounts: Map[String, Long]): Map[String, Long] = {
    newDayCounts.keys.foldLeft(newDayCounts){case (counts, statName) =>
      counts + (s"cumulative-$statName" -> newDayCounts(statName))
    }
  }

}

object CampaignPageViewsReport {

  implicit val ec = AnalyticsDataCache.analyticsExectuionContext

  implicit val campaignPageViewsReportFormat: Format[CampaignPageViewsReport] = Jsonx.formatCaseClass[CampaignPageViewsReport]

  def getCampaignPageViewsReport(campaignId: String): Option[CampaignPageViewsReport] = {

    AnalyticsDataCache.getCampaignPageViewsReport(campaignId) match {
      case Hit(report) => {
        Logger.debug(s"getting page view report for campaign $campaignId - cache hit")
        Some(report)
      }
      case Stale(report) => {
        Logger.debug(s"getting page view report for campaign $campaignId - cache stale spawning async refresh")

        Future{
          Logger.debug(s"async refresh of page view report for campaign $campaignId")
          report.refresh
        } // serve stale but spawn refresh future
        Some(report)
      }
      case Miss => {
        Logger.debug(s"getting page view report for campaign $campaignId - cache miss fetching sync")

        generateReport(campaignId)
      }
    }
  }

  def generateReport(campaignId: String): Option[CampaignPageViewsReport] = {
    for (
      campaign <- CampaignRepository.getCampaign(campaignId);
      startDate <- campaign.startDate
    ) yield {
      val dailyReports = DateBasedReport.calculateDatesToFetch(startDate, DateTime.now).map{ dt =>
        Thread.sleep(1000) // try to avoid rate limiting
        loadCampaignPageViewsForDay(campaign, dt)
      }

      val emptyReport = CampaignPageViewsReport(campaignId, Set(), Nil)
      val report = dailyReports.foldLeft(emptyReport) { case (report: CampaignPageViewsReport, (date: DateTime, dailyViewCounts: DailyViewCounts)) =>
        report.addDayCounts(date, dailyViewCounts)
      }

      AnalyticsDataCache.putCampaignPageViewsReport(campaignId, report, AnalyticsDataCache.calculateValidToDateForDailyStats(campaign))

      report
    }
  }

  def loadCampaignPageViewsForDay(campaign: Campaign, date: DateTime) = {
    val dailyCounts = if (campaign.`type` == "hosted") {
      campaign.gaFilterExpression.flatMap(GoogleAnalytics.loadPageViewsForDay(_, date))
    } else {
      campaign.pathPrefix.flatMap{ section => GoogleAnalytics.loadPageViewsForDay(s"ga:dimension4==${section}", date)}
    }

    date -> calculateDailyTotals(dailyCounts)
  }

  private def calculateDailyTotals(dailyViewCounts: Option[DailyViewCounts]): DailyViewCounts = {
    dailyViewCounts match {
      case Some(DailyViewCounts(seenPaths, countStats)) => {
        val stats = countStats.keySet

        val totalCount = stats.filter(_.startsWith("count")).foldLeft(0L){case(total, k) => total + countStats(k)}
        val totalUnique = stats.filter(_.startsWith("unique")).foldLeft(0L){case(total, k) => total + countStats(k)}

        DailyViewCounts(seenPaths, countStats ++ Map("count-total" -> totalCount, "unique-total" -> totalUnique))
      }
      case None =>
        DailyViewCounts(seenPaths = Set(), countStats = Map("count-total" -> 0L, "unique-total" -> 0L))
    }
  }

}
