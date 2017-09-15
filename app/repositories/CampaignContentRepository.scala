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
import services.Dynamo
import cats.implicits._

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

case class PutContentItemResult(contentItem: ContentItem, putItemOutcome: PutItemOutcome)
case class DeleteCampaignContentResult(campignId: String, deletedItemCount: Int, results: List[DeleteItemOutcome])

object CampaignContentRepository {

  private def getItemsForCampaignId(campaignId: String): List[Item] =
    Dynamo.campaignContentTable.query("campaignId", campaignId).toList

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
    Try[List[DeleteItemOutcome]] {
      for (content <- getItemsForCampaignId(campaignId))
        yield Dynamo.campaignContentTable.deleteItem("campaignId", campaignId, "id", content.getString("id"))
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
    contentItem.toItem match {
      case Left(error) => Left(error)
      case Right(item) =>
        Try(Dynamo.campaignContentTable.putItem(item)) match {
          case Success(putItemOutcome) =>
            Logger.info(s"Successfully put ContentItem $contentItem; ${putItemOutcome.toString}")
            Right(PutContentItemResult(contentItem, putItemOutcome))
          case Failure(exception) =>
            Logger.error(s"Failed to persist content $contentItem", exception)
            Left(ContentItemFailedToPersist(contentItem, exception))
        }
    }
  }

}
