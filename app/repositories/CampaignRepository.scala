package repositories

import com.amazonaws.services.dynamodbv2.document.ScanFilter
import model.{Campaign, CampaignWithSubItems}
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConversions._

class CampaignRepository(dynamo: Dynamo,
                         campaignContentRepository: CampaignContentRepository,
                         campaignNotesRepository: CampaignNotesRepository) {

  def getCampaign(campaignId: String): Option[Campaign] = {
    Option(dynamo.campaignTable.getItem("id", campaignId)).map { Campaign.fromItem }
  }

  def getCampaignByTag(tagId: Long): Option[Campaign] = {
    dynamo.campaignTable.scan(new ScanFilter("tagId").eq(tagId)).headOption.map(Campaign.fromItem)
  }

  def getAllCampaigns(): List[Campaign] = {
    dynamo.campaignTable.scan().map { Campaign.fromItem }.toList
  }

  def getCampaignWithSubItems(campaignId: String): Option[CampaignWithSubItems] = {
    val campaign = Option(dynamo.campaignTable.getItem("id", campaignId)).map { Campaign.fromItem }

    campaign map { c =>
      CampaignWithSubItems(
        campaign = c,
        content = campaignContentRepository.getContentForCampaign(c.id),
        notes = campaignNotesRepository.getNotesForCampaign(c.id)
      )
    }
  }

  def deleteCampaign(campaignId: String): Unit = {
    try {
      dynamo.campaignTable.deleteItem("id", campaignId)
    } catch {
      case e: Error =>
        Logger.error(s"failed to delete campaign $campaignId", e)
        None
    }
  }

  def putCampaign(campaign: Campaign): Option[Campaign] = {
    try {
      dynamo.campaignTable.putItem(campaign.toItem)
      Some(campaign)
    } catch {
      case e: Error =>
        Logger.error(s"failed to persist campaign $campaign", e)
        None
    }
  }

}
