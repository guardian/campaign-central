package repositories

import model.LatestCampaignAnalyticsItem
import services.Dynamo

import scala.collection.JavaConverters._

class LatestCampaignAnalyticsRepository(dynamo: Dynamo) {

  def getLatestCampaignAnalytics(): Seq[LatestCampaignAnalyticsItem] = {
    dynamo.latestCampaignAnalyticsTable.scan().asScala.toList.map(LatestCampaignAnalyticsItem.fromItem)
  }

  def getLatestCampaignAnalytics(campaignId: String): Option[LatestCampaignAnalyticsItem] = {
    dynamo.latestCampaignAnalyticsTable
      .query("campaignId", campaignId)
      .asScala
      .headOption
      .map(LatestCampaignAnalyticsItem.fromItem)
  }

}
