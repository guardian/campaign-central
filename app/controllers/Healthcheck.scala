package controllers

import play.api.mvc.{AbstractController, ControllerComponents}

class Healthcheck(components: ControllerComponents) extends AbstractController(components) {

  def ok = Action {
    Ok("OK")
  }

}
