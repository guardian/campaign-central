package controllers

import com.gu.googleauth.AuthAction
import model.{JsonParsingError, Territory}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
import services.ReportExecutionService

class ReportExecutionApi(components: ControllerComponents, authAction: AuthAction[AnyContent])
  extends AbstractController(components) {

  def getLastUpdatedTime(territory: Option[String]) = authAction { implicit request =>
    val campaignTerritory = Territory(territory getOrElse "global")

    ReportExecutionService.getLastExecutedTime(campaignTerritory) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(lastUpdatedTime)        => Ok(Json.toJson(lastUpdatedTime))
    }
  }

}
