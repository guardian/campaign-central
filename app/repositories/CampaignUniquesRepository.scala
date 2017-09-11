package repositories

import model.CampaignUniquesItem
import services.Dynamo

import scala.collection.JavaConversions._

class CampaignUniquesRepository(dynamo: Dynamo) {

  def getCampaignUniques(campaignId: String): Seq[CampaignUniquesItem] = {
    dynamo.campaignUniquesTable.query("campaignId", campaignId).map(CampaignUniquesItem.fromItem).toList
  }

}
