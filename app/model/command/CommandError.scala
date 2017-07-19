package model.command

import model.Client
import play.api.mvc.{Result, Results}

case class CommandError(message: String, responseCode: Int) extends RuntimeException(message)

object CommandError extends Results {

  def CampaignTagNotFound = new CommandError("campaign tag not found", 400)
  def InvalidCampaignTagType = throw new CommandError("campaign tag was not expected type", 400)
  def CampaignNotFound = throw new CommandError("campaign not found", 404)
  def CampaignMissingData(field: String) = throw new CommandError(s"campaign missing required field $field", 400)
  def SponsorNameNotFound = throw new CommandError("unable to find a sponsor name", 400)
  def UnableToDetermineContentType = throw new CommandError("unable to find a determine content's type", 400)
  def FailedToSaveClient(client: Client) = throw new CommandError(s"failed to save client ${client.name}", 503)


  def commandErrorAsResult: PartialFunction[Throwable, Result] = {
    case CommandError(msg, 400) => BadRequest(msg)
    case CommandError(msg, 404) => NotFound
  }
}
