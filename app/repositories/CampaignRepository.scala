package repositories

import com.gu.scanamo._
import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.command.{CampaignCentralApiError, CampaignDeletionFailed, CampaignPutError}
import model.{Campaign, CampaignWithSubItems}
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.{getResult, getResults}

import scala.util.{Failure, Success, Try}

case class CampaignRepositoryPutResult(campaign: Campaign)
case class CampaignRepositoryDeleteResult(campaignId: String)

object CampaignRepository {

  private implicit val logger: Logger = Logger(getClass)

  private val tableName = Config().campaignTableName

  def getCampaign(campaignId: String): Option[Campaign] = {
    val option = Scanamo.get[Campaign](DynamoClient)(tableName)('id -> campaignId)
    option.flatMap(getResult(_))
  }

  def getCampaignByTag(tagId: Long): Option[Campaign] = {
    val list = Scanamo.query[Campaign](DynamoClient)(tableName)('tagId -> tagId)
    list.headOption.flatMap(getResult(_))
  }

  def getAllCampaigns(): List[Campaign] = getResults(Scanamo.scan[Campaign](DynamoClient)(tableName))

  def getCampaignWithSubItems(campaignId: String): Option[CampaignWithSubItems] =
    getCampaign(campaignId).map { campaign =>
      CampaignWithSubItems(
        campaign = campaign,
        content = CampaignContentRepository.getContentForCampaign(campaign.id)
      )
    }

  def deleteCampaign(campaignId: String): Either[CampaignCentralApiError, CampaignRepositoryDeleteResult] =
    Try(Scanamo.delete(DynamoClient)(tableName)('campaignId -> campaignId)) match {
      case Success(result) =>
        Logger.debug(result.toString)
        Right(CampaignRepositoryDeleteResult(campaignId))
      case Failure(exception) =>
        Logger.error(s"failed to delete campaign $campaignId", exception)
        Left(CampaignDeletionFailed(campaignId, exception))
    }

  def putCampaign(campaign: Campaign): Either[CampaignCentralApiError, CampaignRepositoryPutResult] =
    Try(Scanamo.put[Campaign](DynamoClient)(tableName)(campaign)) match {
      case Success(result) =>
        Logger.debug(result.toString)
        Right(CampaignRepositoryPutResult(campaign))
      case Failure(exception) =>
        Logger.error(s"failed to persist campaign $campaign", exception)
        Left(CampaignPutError(campaign, exception))
    }
}
