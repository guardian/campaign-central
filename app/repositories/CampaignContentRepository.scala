package repositories

import model.ContentItem
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConversions._

class CampaignContentRepository(dynamo: Dynamo) {

  def getContentForCampaign(campaignId: String): List[ContentItem] = {
    dynamo.campaignContentTable.query("campaignId", campaignId).map { ContentItem.fromItem }.toList
  }

  def getContent(campaignId: String, id: String): Option[ContentItem] = {
    Option(dynamo.campaignContentTable.getItem("campaignId", campaignId, "id", id)).map { ContentItem.fromItem }
  }

  def deleteContentForCampaign(campaignId: String): Any = {
    try {
      for (content <- dynamo.campaignContentTable.query("campaignId", campaignId)) {
        dynamo.campaignContentTable.deleteItem("campaignId", campaignId, "id", content.getString("id"))
      }
    } catch {
      case e: Error =>
        Logger.error(s"failed to delete content for campaign $campaignId", e)
        None
    }
  }

  def putContent(content: ContentItem): Option[ContentItem] = {
    try {
      dynamo.campaignContentTable.putItem(content.toItem)
      Some(content)
    } catch {
      case e: Error =>
        Logger.error(s"failed to persist content $content", e)
        None
    }
  }

}
