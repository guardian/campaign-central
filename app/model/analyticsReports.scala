package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format
import repositories.GoogleAnalytics.ParsedDailyCountsReport

case class CampaignDailyCountsReport(seenPaths: Set[String], pageCountStats: List[Map[String, Long]])

object CampaignDailyCountsReport{
  implicit val campaignDailyCountsReportFormat: Format[CampaignDailyCountsReport] = Jsonx.formatCaseClass[CampaignDailyCountsReport]

  def apply(parsedDailyCountsReport: ParsedDailyCountsReport): CampaignDailyCountsReport = {
    CampaignDailyCountsReport(
      parsedDailyCountsReport.seenPaths,
      parsedDailyCountsReport.dayStats.keySet.toList.sortBy(_.getMillis).map{ dt =>
        val stats = parsedDailyCountsReport.dayStats(dt)
        stats + ("date" -> dt.getMillis)
      }
    )
  }
}



