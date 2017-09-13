package repositories

import com.amazonaws.services.dynamodbv2.document.ScanFilter
import model.command.{CampaignCentralApiError, CampaignDeletionFailed, CampaignPutError}
import model.{Campaign, CampaignWithSubItems}
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}


case class CampaignRepositoryPutResult(campaign: Campaign)
case class CampaignRepositoryDeleteResult(campaignId: String)

object CampaignRepository {

  def getCampaign(campaignId: String): Option[Campaign] = {
    Option(Dynamo.campaignTable.getItem("id", campaignId)).map{ Campaign.fromItem }
  }

  def getCampaignByTag(tagId: Long): Option[Campaign] = {
    Dynamo.campaignTable.scan(new ScanFilter("tagId").eq(tagId)).headOption.map( Campaign.fromItem )
  }

  def getAllCampaigns(): List[Campaign] = {
    Dynamo.campaignTable.scan().map{ Campaign.fromItem }.toList
  }

  def getCampaignWithSubItems(campaignId: String): Option[CampaignWithSubItems] = {
    val maybeCampaign: Option[Campaign] = getCampaign(campaignId)

    maybeCampaign.map { campaign =>
      CampaignWithSubItems(
        campaign = campaign,
        content = CampaignContentRepository.getContentForCampaign(campaign.id),
        notes = CampaignNotesRepository.getNotesForCampaign(campaign.id)
      )
    }
  }

  def deleteCampaign(campaignId: String): Either[CampaignCentralApiError, CampaignRepositoryDeleteResult] = {
    Try(Dynamo.campaignTable.deleteItem("id", campaignId)) match {
      case Success(result) =>
        Logger.debug(result.toString)
        Right(CampaignRepositoryDeleteResult(campaignId))
      case Failure(exception) =>
        Logger.error(s"failed to delete campaign $campaignId", exception)
        Left(CampaignDeletionFailed(campaignId, exception))
    }
  }

  def putCampaign(campaign: Campaign): Either[CampaignCentralApiError, CampaignRepositoryPutResult] = {
    Try(Dynamo.campaignTable.putItem(campaign.toItem)) match {
      case Success(result) =>
        Logger.debug(result.toString)
        Right(CampaignRepositoryPutResult(campaign))
      case Failure(exception) =>
        Logger.error(s"failed to persist campaign $campaign", exception)
        Left(CampaignPutError(campaign, exception))
    }
  }

}
