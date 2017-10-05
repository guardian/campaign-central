package controllers

import java.util.concurrent.Executors

import model.{JsonParsingError, User}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{CampaignService, Config}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class ManagementApi(components: ControllerComponents) extends AbstractController(components) {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  implicit val user                         = User("Campaign Central", "Admin", "commercial.dev@theguardian.com")

  val Stage = Config.conf.stage.toUpperCase

  def refreshCampaigns = Action { implicit request =>
    val isRefreshCampaignAllowed: Boolean = request.queryString.get("api-key").flatMap(_.headOption) match {
      case Some(apiKey) if apiKey == Config.conf.campaignCentralApiKey && Stage == "PROD" => true
      case Some(_) | None if Stage == "DEV"                                          => true
      case _                                                                         => false
    }

    if (isRefreshCampaignAllowed) {
      CampaignService.synchroniseCampaigns() match {
        case Left(error: JsonParsingError) => BadRequest(error.message)
        case Left(_)                       => InternalServerError
        case Right(_)                      => NoContent
      }
    } else {
      Unauthorized
    }

  }

}
