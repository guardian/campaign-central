package controllers

import com.gu.googleauth.AuthAction
import net.logstash.logback.argument.StructuredArguments.value
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import services.Config
import services.LogShipping.logMessageAndCustomField

class App(components: ControllerComponents, authAction: AuthAction[AnyContent])
  extends AbstractController(components) {

  private lazy val logger = Logger(this.getClass)

  def index(id: String = "") = authAction { implicit request =>
    logMessageAndCustomField(
      logger,
      message = "{} accessed Campaign Central.",
      field = value("accessedBy", request.user.email)
    )

    val jsFileName = "build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None            => routes.Assets.versioned(jsFileName).toString
    }

    val clientConf = Map(
      "tagManagerUrl" -> Config().tagManagerApiUrl,
      "composerUrl"   -> Config().composerUrl,
      "liveUrl"       -> Config().liveUrl,
      "previewUrl"    -> Config().previewUrl
    )

    Ok(views.html.Application.app("Campaign Central", jsLocation, Json.toJson(clientConf).toString))
  }
}
