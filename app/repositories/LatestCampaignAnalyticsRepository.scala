package repositories

import model.LatestCampaignAnalyticsItem
import services.Dynamo
import scala.collection.JavaConverters._

object LatestCampaignAnalyticsRepository {

  def getLatestCampaignAnalytics(): Seq[LatestCampaignAnalyticsItem] = {
    Dynamo.latestCampaignAnalyticsTable.scan().asScala.toList.map(LatestCampaignAnalyticsItem.fromItem)
  }

  def getLatestCampaignAnalytics(campaignId: String): Option[LatestCampaignAnalyticsItem] = {
    Dynamo.latestCampaignAnalyticsTable.query("campaignId", campaignId).asScala.headOption.map(LatestCampaignAnalyticsItem.fromItem)
  }

}
