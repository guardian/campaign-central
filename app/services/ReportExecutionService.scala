package services

import model.{CampaignCentralApiError, LastExecuted, Territory}
import org.joda.time.DateTime
import repositories.LatestCampaignAnalyticsRepository

object ReportExecutionService {

  def getLastExecutedTime(territory: Territory): Either[CampaignCentralApiError, Option[LastExecuted]] = {
    for {
      latestCampaignAnalytics <- LatestCampaignAnalyticsRepository.getLatestCampaignAnalytics(territory)
    } yield {
      latestCampaignAnalytics
        .map(lca => new DateTime(lca.reportExecutionTimestamp))
        .sortWith((dt1, dt2) => dt1.isAfter(dt2))
        .headOption
        .map(dt => LastExecuted(dt.toString))
    }
  }

}
