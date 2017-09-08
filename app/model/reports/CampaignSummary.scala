package model.reports

import ai.x.play.json.Jsonx
import model.Campaign
import org.joda.time.{DateTime, Duration}
import play.api.Logger
import play.api.libs.json.Format
import repositories.{AnalyticsDataCache, Hit, Miss, Stale}

case class CampaignSummary(totalUniques: Long, targetToDate: Long)

object CampaignSummary {

  implicit val campaignSummaryFormat: Format[CampaignSummary] = Jsonx.formatCaseClass[CampaignSummary]

  def getCampaignSummary(analyticsDataCache: AnalyticsDataCache, campaignId: String): Option[CampaignSummary] = {
    analyticsDataCache.getCampaignSummary(campaignId) match {
      case Hit(report) =>
        Logger.debug(s"getting campaign summary for campaign $campaignId - cache hit")
        Some(report)
      case Stale(report) =>
        Logger.debug(s"getting campaign summary for campaign $campaignId - cache stale")
        Some(report)
      case Miss =>
        Logger.debug(s"getting campaign summary for campaign $campaignId - cache miss")
        None
    }
  }

  def storeLatestUniquesForCampaign(
    analyticsDataCache: AnalyticsDataCache,
    campaign: Campaign,
    latestUniquesOption: Option[DailyUniqueUserEntry]
  ): Unit = {
    latestUniquesOption.foreach { latestUniques =>
      val targetToDate = calculateTargetToDate(campaign, latestUniques.date)
      val summary      = CampaignSummary(latestUniques.cumulativeUniqueUsers, targetToDate)

      analyticsDataCache.putCampaignSummary(campaign.id,
                                            summary,
                                            analyticsDataCache.calculateValidToDateForDailyStats(campaign))

      OverallSummaryReport.storeLatestUniquesForCampaign(analyticsDataCache, campaign.id, summary)
    }
  }

  def calculateTargetToDate(campaign: Campaign, date: DateTime): Long = {
    val targetToDate = for (startDate <- campaign.startDate;
                            endDate      <- campaign.endDate;
                            uniqueTarget <- campaign.targets.get("uniques")) yield {

      val campaignLength = new Duration(startDate, endDate).getStandardDays
      val daysIn         = new Duration(startDate, date).getStandardDays

      val dailyRunRate = uniqueTarget.toDouble / campaignLength

      (daysIn * dailyRunRate).toLong
    }

    targetToDate.getOrElse(0L)
  }
}
