package repositories

import model.CampaignPageViewsItem
import services.Dynamo

import scala.collection.JavaConversions._

class CampaignPageViewsRepository(dynamo: Dynamo) {

  def getCampaignPageViews(campaignId: String): List[CampaignPageViewsItem] = {
    dynamo.campaignPageviewsTable.query("campaignId", campaignId).map { CampaignPageViewsItem.fromItem }.toList
  }

}
