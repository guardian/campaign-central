package controllers

import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import services.Config

import scala.concurrent.Future

class App(override val wsClient: WSClient, components: ControllerComponents)
  extends CentralController(components) with PandaAuthActions {

  def index(id: String = "") = AuthAction { implicit request =>

    val jsFileName = "build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    val clientConf = Map(
      "tagManagerUrl" -> Config().tagManagerApiUrl,
      "composerUrl" -> Config().composerUrl,
      "liveUrl" -> Config().liveUrl,
      "previewUrl" -> Config().previewUrl,
      "mediaAtomMakerUrl" -> Config().mediaAtomMakerUrl,
      "ctaAtomMakerUrl" -> Config().ctaAtomMakerUrl
    )

    Ok(views.html.Application.app("Campaign Central", jsLocation, Json.toJson(clientConf).toString()))
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }

  def oauthCallback = Action.async { implicit request =>
    processGoogleCallback()
  }

  def logout = Action.async { implicit request =>
    Future(processLogout)(components.executionContext)
  }
}
