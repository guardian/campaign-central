package repositories

import cats.implicits._
import com.gu.scanamo.syntax._
import com.gu.scanamo.{DynamoFormat, Scanamo, Table}
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

  private val table = Table[Campaign](Config().campaignTableName)

  implicit val jodaLongFormat: DynamoFormat[DateTime] = DynamoFormat.xmap[DateTime, Long] { epochMillis =>
    Right(new DateTime(epochMillis).withZone(UTC))
  } { dateTime =>
    dateTime.withZone(UTC).getMillis
  }

  def getCampaign(campaignId: String): Either[CampaignCentralApiError, Campaign] = {
    Scanamo.exec(DynamoClient)(table.get('id -> campaignId)) map {
      case Left(e)         => Left(JsonParsingError(e.show))
      case Right(campaign) => Right(campaign)
    } getOrElse
      Left(CampaignNotFound(s"Campaign with id $campaignId could not be found."))
  }

  def getCampaignByTag(tagId: Long): Either[CampaignCentralApiError, Campaign] = {
    Scanamo.exec(DynamoClient)(table.get('tagId -> tagId)) map {
      case Left(e)         => Left(JsonParsingError(e.show))
      case Right(campaign) => Right(campaign)
    } getOrElse
      Left(CampaignNotFound(s"Campaign could not be found by tag id $tagId"))
  }

  def getAllCampaigns(): Either[CampaignCentralApiError, List[Campaign]] =
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.scan)).leftMap { e =>
      JsonParsingError(e.show)
    }

  def getCampaignWithSubItems(campaignId: String): Either[CampaignCentralApiError, CampaignWithSubItems] =
    for {
      campaign <- getCampaign(campaignId)
      content  <- CampaignContentRepository.getContentForCampaign(campaign.id)
    } yield CampaignWithSubItems(campaign, content)

  def deleteCampaign(campaignId: String): Either[CampaignCentralApiError, CampaignRepositoryDeleteResult] =
    Try(Scanamo.exec(DynamoClient)(table.delete('campaignId -> campaignId))) match {
      case Success(result) =>
        Logger.debug(result.toString)
        Right(CampaignRepositoryDeleteResult(campaignId))
      case Failure(exception) =>
        Logger.error(s"failed to delete campaign $campaignId", exception)
        Left(CampaignDeletionFailed(campaignId, exception))
    }

  def putCampaign(campaign: Campaign): Either[CampaignCentralApiError, CampaignRepositoryPutResult] =
    Try(Scanamo.exec(DynamoClient)(table.put(campaign))) match {
      case Success(result) =>
        Logger.debug(result.toString)
        Right(CampaignRepositoryPutResult(campaign))
      case Failure(exception) =>
        Logger.error(s"failed to persist campaign $campaign", exception)
        Left(CampaignPutError(campaign, exception))
    }
}
