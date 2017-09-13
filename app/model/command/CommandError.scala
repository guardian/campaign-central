package model.command

import model.{Campaign, Client, ContentItem}
import play.api.mvc.{Result, Results}

sealed trait CampaignCentralApiError
case class CampaignNotFound(message: String)                                          extends CampaignCentralApiError
case class ContentItemFailedToPersist(contentItem: ContentItem, exception: Throwable) extends CampaignCentralApiError
case class CampaignDeletionFailed(campaignId: String, exception: Throwable)           extends CampaignCentralApiError
case class CampaignItemDeletionFailed(campaignId: String, exception: Throwable)       extends CampaignCentralApiError
case class CampaignPutError(campaign: Campaign, exception: Throwable)                 extends CampaignCentralApiError
case class CampaignTagNotFound(id: Long, externalName: String)                        extends CampaignCentralApiError

case class CommandError(message: String, responseCode: Int) extends RuntimeException(message)

object CommandError extends Results {

  def InvalidCampaignTagType             = throw new CommandError("campaign tag was not expected type", 400)
  def CampaignMissingData(field: String) = throw new CommandError(s"campaign missing required field $field", 400)
  def SponsorNameNotFound                = throw new CommandError("unable to find a sponsor name", 400)
  def FailedToSaveClient(client: Client) = throw new CommandError(s"failed to save client ${client.name}", 503)

  def commandErrorAsResult: PartialFunction[Throwable, Result] = {
    case CommandError(msg, 400) => BadRequest(msg)
    case CommandError(msg, 404) => NotFound
  }
}
