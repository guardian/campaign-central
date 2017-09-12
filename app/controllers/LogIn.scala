package controllers

import com.gu.googleauth.{GoogleAuthConfig, LoginSupport}
import play.api.libs.ws.WSClient
import play.api.mvc.{BaseController, Call, ControllerComponents}

import scala.concurrent.ExecutionContext

// based on https://github.com/guardian/play-googleauth/blob/master/example/app/controllers/Login.scala
class LogIn(
  override val wsClient: WSClient,
  val controllerComponents: ControllerComponents,
  val authConfig: GoogleAuthConfig
)(implicit executionContext: ExecutionContext)
  extends LoginSupport
  with BaseController {

  def logIn() = Action.async { implicit request =>
    startGoogleLogin()
  }

  def oauth2Callback() = Action.async { implicit request =>
    processOauth2Callback()
  }

  def logOut() = Action { implicit request =>
    Redirect(routes.App.index("")).withNewSession
  }

  override val failureRedirectTarget: Call = routes.LogIn.logOut()
  override val defaultRedirectTarget: Call = routes.App.index("")
}
