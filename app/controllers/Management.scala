package controllers

import play.api.mvc.{AbstractController, ControllerComponents}

class Management(components: ControllerComponents) extends AbstractController(components) {

  def healthCheck = Action {
    Ok("OK")
  }

}
