package controllers

import play.api.mvc._

class App extends Controller {

  def hello = Action {
    Ok("hello world")
  }

  def index = Action {

    val jsFileName = "build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    Ok(views.html.Application.app("Campaign Central", jsLocation))
  }
}

