package repositories

<<<<<<< HEAD
import com.gu.scanamo._
import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.command.{CampaignCentralApiError, CampaignDeletionFailed, CampaignPutError}
import model.{Campaign, CampaignWithSubItems}
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.{getResult, getResults}
=======
import com.amazonaws.services.dynamodbv2.document.ScanFilter
import model.command._
import model.{Campaign, CampaignWithSubItems}
import play.api.Logger
import services.Dynamo
import cats.implicits._
>>>>>>> master

import scala.util.{Failure, Success, Try}

case class CampaignRepositoryPutResult(campaign: Campaign)
case class CampaignRepositoryDeleteResult(campaignId: String)

object CampaignRepository {

<<<<<<< HEAD
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
=======
  def getCampaign(campaignId: String): Either[CampaignCentralApiError, Campaign] = {
    val maybeCampaignOrError: Either[CampaignCentralApiError, Option[Campaign]] = {
      val result = Option(Dynamo.campaignTable.getItem("id", campaignId)).map { Campaign.fromItem }
      result.sequence
    }

    maybeCampaignOrError.flatMap { maybeCampaign =>
      maybeCampaign.map(Right(_)) getOrElse Left(CampaignNotFound(s"Campaign with id $campaignId could not be found."))
    }
  }

  def getCampaignByTag(tagId: Long): Either[CampaignCentralApiError, Campaign] = {
    val campaignOrError: Either[CampaignCentralApiError, Option[Campaign]] = {
      val result: Option[Either[CampaignCentralApiError, Campaign]] =
        Dynamo.campaignTable.scan(new ScanFilter("tagId").eq(tagId)).headOption.map(Campaign.fromItem)
      result.sequence
    }

    campaignOrError.flatMap(maybeCampaign =>
      maybeCampaign.map(Right(_)) getOrElse Left(CampaignNotFound(s"Campaign could not be found by tag id $tagId")))
  }

  def getAllCampaigns(): Either[CampaignCentralApiError, List[Campaign]] = {
    val result: List[Either[CampaignCentralApiError, Campaign]] =
      Dynamo.campaignTable.scan().map { Campaign.fromItem }.toList
    result.sequence
  }

  def getCampaignWithSubItems(campaignId: String): Either[CampaignCentralApiError, CampaignWithSubItems] = {
    for {
      campaign <- getCampaign(campaignId)
      content  <- CampaignContentRepository.getContentForCampaign(campaign.id)
    } yield {
      CampaignWithSubItems(campaign, content)
>>>>>>> master
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

<<<<<<< HEAD
  def putCampaign(campaign: Campaign): Either[CampaignCentralApiError, CampaignRepositoryPutResult] =
    Try(Scanamo.put[Campaign](DynamoClient)(tableName)(campaign)) match {
      case Success(result) =>
        Logger.debug(result.toString)
        Right(CampaignRepositoryPutResult(campaign))
      case Failure(exception) =>
        Logger.error(s"failed to persist campaign $campaign", exception)
        Left(CampaignPutError(campaign, exception))
    }
=======
  def putCampaign(campaign: Campaign): Either[CampaignCentralApiError, CampaignRepositoryPutResult] = {

    campaign.toItem match {
      case Left(error) => Left(error)
      case Right(item) =>
        Try(Dynamo.campaignTable.putItem(item)) match {
          case Success(result) =>
            Logger.debug(result.toString)
            Right(CampaignRepositoryPutResult(campaign))

          case Failure(exception) =>
            Logger.error(s"failed to persist campaign $campaign", exception)
            Left(CampaignPutError(campaign, exception))
        }
    }
  }
>>>>>>> master
}
