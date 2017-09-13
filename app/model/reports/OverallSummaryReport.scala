package model.reports

import ai.x.play.json.Jsonx
import play.api.Logger
import play.api.libs.json.Format
import repositories.{AnalyticsDataCache, Hit, Miss, Stale}

case class OverallSummaryReport(summaries: Map[String, CampaignSummary]) {
  def storeLatestUniquesForCampaign(campaignId: String, summary: CampaignSummary) = {
    OverallSummaryReport(summaries + (campaignId -> summary))
  }
}

object OverallSummaryReport {
  implicit val overallSummaryReportFormat: Format[OverallSummaryReport] = Jsonx.formatCaseClass[OverallSummaryReport]

  def getOverallSummaryReport(): Option[OverallSummaryReport] = {
    AnalyticsDataCache.getOverallSummary() match {
      case Hit(report) => {
        Logger.debug("getting overall summary - cache hit")
        Some(report)
      }
      case Stale(report) => {
        Logger.debug("getting overall summary - cache stale")
        Some(report)
      }
      case Miss => {
        Logger.debug("getting overall summary - cache miss")
        None
      }
    }
  }

  def storeLatestUniquesForCampaign(campaignId: String, summary: CampaignSummary) = {
    val report  = getOverallSummaryReport.getOrElse(OverallSummaryReport(Map()))
    val updated = report.storeLatestUniquesForCampaign(campaignId: String, summary: CampaignSummary)

    AnalyticsDataCache.putOverallSummary(updated)
  }
}
