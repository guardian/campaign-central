package controllers

import play.api.mvc.{Action, Controller}

class Management extends Controller {

  def healthCheck = Action {
    Ok("OK")
  }

}

