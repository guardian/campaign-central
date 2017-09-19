package repositories

import cats.implicits._
import com.amazonaws.services.dynamodbv2.model.{DeleteItemResult, PutItemResult}
import com.gu.scanamo.{Scanamo, Table}
import com.gu.scanamo.syntax._
import model.ContentItem
import model.command._
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

import scala.util.{Failure, Success, Try}

case class PutContentItemResult(contentItem: ContentItem, putItemResult: PutItemResult)
case class DeleteCampaignContentResult(campignId: String, deletedItemCount: Int, results: List[DeleteItemResult])

object CampaignContentRepository {

  private val table = Table[ContentItem](Config().campaignContentTableName)

  def getContentForCampaign(campaignId: String): Either[CampaignCentralApiError, List[ContentItem]] = {
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.query('campaignId -> campaignId))).leftMap { e =>
      JsonParsingError(e.show)
    }
  }

  def getContent(campaignId: String, id: String): Either[CampaignCentralApiError, ContentItem] =
    Scanamo.exec(DynamoClient)(table.get('campaignId -> campaignId and 'id -> id)) map {
      case Left(e)            => Left(JsonParsingError(e.show))
      case Right(contentItem) => Right(contentItem)
    } getOrElse
      Left(ContentItemNotFound(s"Could not find content item with campaign id $campaignId and id $id"))

  def deleteContentForCampaign(campaignId: String): Either[CampaignCentralApiError, DeleteCampaignContentResult] =
    getContentForCampaign(campaignId) match {
      case Left(e) => Left(e)
      case Right(content) =>
        Try {
          for (item <- content)
            yield Scanamo.exec(DynamoClient)(table.delete('campaignId -> campaignId and 'id -> item.id))
        } match {
          case Success(results) =>
            Logger.info(s"Deleted ${results.length} items for $campaignId")
            Right(DeleteCampaignContentResult(campaignId, results.length, results))
          case Failure(exception) =>
            Logger.error(s"failed to delete content for campaign $campaignId", exception)
            Left(CampaignItemDeletionFailed(campaignId, exception))
        }
    }

  def putContent(contentItem: ContentItem): Either[CampaignCentralApiError, PutContentItemResult] =
    Try(Scanamo.exec(DynamoClient)(table.put(contentItem))) match {
      case Success(putItemResult) =>
        Logger.info(s"Successfully put ContentItem $contentItem; ${putItemResult.toString}")
        Right(PutContentItemResult(contentItem, putItemResult))
      case Failure(exception) =>
        Logger.error(s"Failed to persist content $contentItem", exception)
        Left(ContentItemFailedToPersist(contentItem, exception))
    }
}
