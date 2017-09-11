package controllers

import com.gu.googleauth.AuthAction
import play.api.libs.json.Json
import play.api.mvc._
import services.Config

class App(components: ControllerComponents, authAction: AuthAction[AnyContent])
  extends AbstractController(components) {

  def index(id: String = "") = authAction { implicit request =>
    val jsFileName = "build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None            => routes.Assets.versioned(jsFileName).toString
    }

    val clientConf = Map(
      "tagManagerUrl"     -> Config().tagManagerApiUrl,
      "composerUrl"       -> Config().composerUrl,
      "liveUrl"           -> Config().liveUrl,
      "previewUrl"        -> Config().previewUrl,
      "mediaAtomMakerUrl" -> Config().mediaAtomMakerUrl,
      "ctaAtomMakerUrl"   -> Config().ctaAtomMakerUrl
    )

    Ok(views.html.Application.app("Campaign Central", jsLocation, Json.toJson(clientConf).toString()))
  }
}
