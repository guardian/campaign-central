package repositories

import model.{CampaignUniquesItem}
import services.Dynamo

import scala.collection.JavaConversions._

object CampaignUniquesRepository {
  def getCampaignUniques(campaignId: String) = {
    Dynamo.campaignUniquesTable.query("campaignId", campaignId).map{ CampaignUniquesItem.fromItem }.toList
  }
}
