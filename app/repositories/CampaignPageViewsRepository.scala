package repositories

import model.{CampaignPageViewsItem}
import services.Dynamo

import scala.collection.JavaConversions._

object CampaignPageViewsRepository {
  def getCampaignPageViews(campaignId: String) = {
    Dynamo.campaignPageviewsTable.query("campaignId", campaignId).map{ CampaignPageViewsItem.fromItem }.toList
  }
}
