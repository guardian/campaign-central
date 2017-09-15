package repositories

import com.amazonaws.services.dynamodbv2.model.{DeleteItemResult, PutItemResult}
import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.ContentItem
import model.command.{CampaignCentralApiError, CampaignItemDeletionFailed, ContentItemFailedToPersist}
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults

import scala.util.{Failure, Success, Try}

case class PutContentItemResult(contentItem: ContentItem, putItemResult: PutItemResult)
case class DeleteCampaignContentResult(campignId: String, deletedItemCount: Int, results: List[DeleteItemResult])

object CampaignContentRepository {

  private implicit val logger: Logger = Logger(getClass)

  private val tableName = Config().campaignContentTableName

  def getContentForCampaign(campaignId: String): List[ContentItem] =
    getResults(Scanamo.query[ContentItem](DynamoClient)(tableName)('campaignId -> campaignId))

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
