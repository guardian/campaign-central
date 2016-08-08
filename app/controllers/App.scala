package controllers

import play.api.mvc._

class App extends Controller {

  def hello = Action {
    Ok("hello world")
  }
}

