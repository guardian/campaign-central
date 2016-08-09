package controllers

import model._
import play.api.libs.json._
import play.api.mvc.{Controller, Action}

class CampaignApi extends Controller {

  private val tempCampaignList = List(
    Campaign("1", "Test Campaign 1", Client("a", "Test Client A", "UK")),
    Campaign("2", "Test Campaign 2", Client("b", "Test Client B", "AU")),
    Campaign("3", "Test Campaign 3", Client("c", "Test Client C", "AU"))
  )


  def getAllCampaigns() = Action { req =>
    Ok(Json.toJson(tempCampaignList))
  }

  def getCampaign(id: String) = Action { req =>

    tempCampaignList.find(_.id == id) match {
      case Some(c) => Ok(Json.toJson(c))
      case None => NotFound
    }
  }
}
