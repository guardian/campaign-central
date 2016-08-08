package controllers

import play.api.mvc._

class App extends Controller {

  def hello = Action {
    Ok("hello world")
  }

  def index = Action {
    Ok(views.html.Application.app("Campaign Central"))
  }
}

