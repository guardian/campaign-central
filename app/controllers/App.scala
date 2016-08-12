package controllers

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class App(override val wsClient: WSClient) extends Controller with PandaAuthActions {

  def hello = AuthAction {
    Ok("hello world")
  }

  def index(id: String = "") = AuthAction {

    val jsFileName = "build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    Ok(views.html.Application.app("Campaign Central", jsLocation))
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }

  def oauthCallback = Action.async { implicit request =>
    processGoogleCallback()
  }

  def logout = Action.async { implicit request =>
    Future(processLogout)
  }
}

