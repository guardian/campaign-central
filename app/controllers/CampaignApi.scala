package controllers

import model._
import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

class CampaignApi(override val wsClient: WSClient) extends Controller with PandaAuthActions {

  private val tempUser = User("Temp", "User", "temp@user.fake")

  private val tempCampaignList = List(
    Campaign("1", "Test Campaign 1", Client("a", "Test Client A", "UK"), DateTime.now, tempUser, DateTime.now, tempUser),
    Campaign("2", "Test Campaign 2", Client("b", "Test Client B", "AU"), DateTime.now, tempUser, DateTime.now, tempUser),
    Campaign("3", "Test Campaign 3", Client("c", "Test Client C", "AU"), DateTime.now, tempUser, DateTime.now, tempUser)
  )


  def getAllCampaigns() = APIAuthAction { req =>
    Ok(Json.toJson(tempCampaignList))
  }

  def getCampaign(id: String) = APIAuthAction { req =>
    tempCampaignList.find(_.id == id) match {
      case Some(c) => Ok(Json.toJson(c))
      case None => NotFound
    }
  }
}
