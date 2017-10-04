package controllers

import java.util.concurrent.Executors

import model.{JsonParsingError, User}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
import services.CampaignService
import com.gu.googleauth.AuthAction

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class ManagementApi(components: ControllerComponents, authAction: AuthAction[AnyContent])
  extends AbstractController(components) {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  implicit val user                         = User("Campaign Central", "Admin", "commercial.dev@theguardian.com")

  def refreshCampaigns = Action {
    CampaignService.synchroniseCampaigns() match {
      case Left(error: JsonParsingError) => BadRequest(error.message)
      case Left(_)                       => InternalServerError
      case Right(_)                      => NoContent
    }

  }

}
