package controllers

import java.util.concurrent.Executors
import model.{JsonParsingError, User}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{CampaignService, Config}
import scala.concurrent.duration._
import play.api.libs.concurrent.Futures._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class ManagementApi(components: ControllerComponents) extends AbstractController(components) {

  implicit val defaultFutures               = new play.api.libs.concurrent.DefaultFutures(akka.actor.ActorSystem())
  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  implicit val user                         = User("Campaign Central", "Admin", "commercial.dev@theguardian.com")

  val Stage = Config.conf.stage.toUpperCase

  def refreshCampaigns = Action.async { implicit request =>

    val isRefreshCampaignAllowed: Boolean = request.queryString.get("api-key").flatMap(_.headOption) match {
      case Some(apiKey) if apiKey == Config.conf.campaignCentralApiKey && Stage == "PROD" => true
      case Some(_) | None if Stage == "DEV"                                               => true
      case _                                                                              => false
    }

    if (isRefreshCampaignAllowed) {
      Future(CampaignService.synchroniseCampaigns())
        .withTimeout(5.seconds)
        .map {
          case Left(error: JsonParsingError) => BadRequest(error.message)
          case Left(_)                       => InternalServerError
          case Right(_)                      => NoContent
        }
        .recover {
          case e: scala.concurrent.TimeoutException =>
            NoContent // If it timeouts out still return a success response for now. This code will eventually be moved out of here and into the calling Lambda.
        }
    } else {
      Future.successful(Unauthorized)
    }
  }

}
