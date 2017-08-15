package repositories

import model.CampaignAnalyticsLatestItem
import services.Dynamo
import scala.collection.JavaConverters._

object CampaignAnalyticsLatestRepository {

  def getLatestCampaignAnalytics(): Seq[CampaignAnalyticsLatestItem] = {
    Dynamo.campaignAnalyticsLatestTable.scan().asScala.toList.map(CampaignAnalyticsLatestItem.fromItem)
  }

  def getLatestCampaignAnalytics(campaignId: String): Option[CampaignAnalyticsLatestItem] = {
    Dynamo.campaignAnalyticsLatestTable.query("campaignId", campaignId).asScala.headOption.map(CampaignAnalyticsLatestItem.fromItem)
  }

}
