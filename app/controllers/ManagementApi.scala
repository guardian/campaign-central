package controllers

import com.gu.pandomainauth.model.{User => PandaUser}
import model._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import repositories.{AnalyticsDataCache, ClientRepository}

class ManagementApi(override val wsClient: WSClient) extends Controller with PandaAuthActions {

  def getAnalyticsCacheSummary() = APIAuthAction { req =>
    Ok(Json.toJson(AnalyticsDataCache.summariseContents))
  }

}
