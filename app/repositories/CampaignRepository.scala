package repositories

import cats.implicits._
import com.gu.scanamo.error.DynamoReadError
import com.gu.scanamo.syntax._
import com.gu.scanamo.{DynamoFormat, Scanamo}
import model.command.{CampaignCentralApiError, CampaignDeletionFailed, CampaignPutError, _}
import model.{Campaign, CampaignWithSubItems}
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

import scala.util.{Failure, Success, Try}

case class CampaignRepositoryPutResult(campaign: Campaign)
case class CampaignRepositoryDeleteResult(campaignId: String)

object CampaignRepository {

  implicit val jodaLongFormat: DynamoFormat[DateTime] = DynamoFormat.xmap[DateTime, Long] { epochMillis =>
    Right(new DateTime(epochMillis).withZone(UTC))
  } { dateTime =>
    dateTime.withZone(UTC).getMillis
  }

  private val tableName = Config().campaignTableName

  def getCampaign(campaignId: String): Either[CampaignCentralApiError, Campaign] = {
    val result: Option[Either[DynamoReadError, Campaign]] =
      Scanamo.get[Campaign](DynamoClient)(tableName)('id -> campaignId)
    result map {
      case Left(e)         => Left(JsonParsingError(e.show))
      case Right(campaign) => Right(campaign)
    } getOrElse
      Left(CampaignNotFound(s"Campaign with id $campaignId could not be found."))
  }

  def getCampaignByTag(tagId: Long): Either[CampaignCentralApiError, Campaign] = {
    val result: Option[Either[DynamoReadError, Campaign]] =
      Scanamo.get[Campaign](DynamoClient)(tableName)('tagId -> tagId)
    result map {
      case Left(e)         => Left(JsonParsingError(e.show))
      case Right(campaign) => Right(campaign)
    } getOrElse
      Left(CampaignNotFound(s"Campaign could not be found by tag id $tagId"))
  }

  def getAllCampaigns(): Either[CampaignCentralApiError, List[Campaign]] =
    getResultsOrFirstFailure(Scanamo.scan[Campaign](DynamoClient)(tableName)).left map { e =>
      JsonParsingError(e.show)
    }

  def getCampaignWithSubItems(campaignId: String): Either[CampaignCentralApiError, CampaignWithSubItems] =
    for {
      campaign <- getCampaign(campaignId)
      content  <- CampaignContentRepository.getContentForCampaign(campaign.id)
    } yield CampaignWithSubItems(campaign, content)

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
