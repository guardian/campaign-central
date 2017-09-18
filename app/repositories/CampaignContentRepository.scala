package repositories

import com.amazonaws.services.dynamodbv2.document.{DeleteItemOutcome, Item, PutItemOutcome}
import model.ContentItem
import model.command.{
  CampaignCentralApiError,
  CampaignItemDeletionFailed,
  ContentItemFailedToPersist,
  ContentItemNotFound
}
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults
import cats.implicits._

import scala.util.{Failure, Success, Try}

case class PutContentItemResult(contentItem: ContentItem, putItemResult: PutItemResult)
case class DeleteCampaignContentResult(campignId: String, deletedItemCount: Int, results: List[DeleteItemResult])

object CampaignContentRepository {

  private implicit val logger: Logger = Logger(getClass)

  private val tableName = Config().campaignContentTableName

  def getContentForCampaign(campaignId: String): List[ContentItem] =
    getResults(Scanamo.query[ContentItem](DynamoClient)(tableName)('campaignId -> campaignId))
  def getContentForCampaign(campaignId: String): Either[CampaignCentralApiError, List[ContentItem]] = {
    val result: List[Either[CampaignCentralApiError, ContentItem]] = getItemsForCampaignId(campaignId).map {
      ContentItem.fromItem
    }
    result.sequence
  }

  def getContent(campaignId: String, id: String): Either[CampaignCentralApiError, ContentItem] = {
    val contentItemOrError: Either[CampaignCentralApiError, Option[ContentItem]] = {
      val result: Option[Either[CampaignCentralApiError, ContentItem]] =
        Option(Dynamo.campaignContentTable.getItem("campaignId", campaignId, "id", id)).map { ContentItem.fromItem }
      result.sequence
    }

    contentItemOrError.flatMap(
      item =>
        item.map(Right(_)) getOrElse Left(
          ContentItemNotFound(s"Could not find content item with campaign id $campaignId and id $id")))
  }

  def deleteContentForCampaign(campaignId: String): Either[CampaignCentralApiError, DeleteCampaignContentResult] = {
    Try {
      for (content <- getContentForCampaign(campaignId))
        yield Scanamo.delete(DynamoClient)(tableName)('campaignId -> campaignId and 'id -> content.id)
    } match {
      case Success(results) =>
        Logger.info(s"Deleted ${results.length} items for $campaignId")
        Right(DeleteCampaignContentResult(campaignId, results.length, results))
      case Failure(exception) =>
        Logger.error(s"failed to delete content for campaign $campaignId", exception)
        Left(CampaignItemDeletionFailed(campaignId, exception))
    }
  }

  def putContent(contentItem: ContentItem): Either[CampaignCentralApiError, PutContentItemResult] = {
    Try(Scanamo.put(DynamoClient)(tableName)(contentItem)) match {
      case Success(putItemResult) =>
        Logger.info(s"Successfully put ContentItem $contentItem; ${putItemResult.toString}")
        Right(PutContentItemResult(contentItem, putItemResult))
      case Failure(exception) =>
        Logger.error(s"Failed to persist content $contentItem", exception)
        Left(ContentItemFailedToPersist(contentItem, exception))
    }
  }
}
