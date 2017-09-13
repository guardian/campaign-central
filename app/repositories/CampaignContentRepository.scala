package repositories

import model.ContentItem
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConversions._

object CampaignContentRepository {

  def getContentForCampaign(campaignId: String) = {
    Dynamo.campaignContentTable.query("campaignId", campaignId).map { ContentItem.fromItem }.toList
  }

  def getContent(campaignId: String, id: String) = {
    Option(Dynamo.campaignContentTable.getItem("campaignId", campaignId, "id", id)).map { ContentItem.fromItem }
  }

  def deleteContentForCampaign(campaignId: String) = {
    try {
      for (content <- Dynamo.campaignContentTable.query("campaignId", campaignId)) {
        Dynamo.campaignContentTable.deleteItem("campaignId", campaignId, "id", content.getString("id"))
      }
    } catch {
      case e: Error => {
        Logger.error(s"failed to delete content for campaign $campaignId", e)
        None
      }
    }
  }

  def putContent(content: ContentItem) = {
    try {
      Dynamo.campaignContentTable.putItem(content.toItem)
      Some(content)
    } catch {
      case e: Error => {
        Logger.error(s"failed to persist content $content", e)
        None
      }
    }
  }

}
