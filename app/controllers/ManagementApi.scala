package controllers

import java.util.concurrent.Executors

import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import repositories.{AnalyticsDataCache, GoogleAnalytics}

import scala.concurrent.{ExecutionContext, Future}

class ManagementApi(override val wsClient: WSClient) extends Controller with PandaAuthActions {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def getAnalyticsCacheSummary() = APIAuthAction { req =>
    Ok(Json.toJson(AnalyticsDataCache.summariseContents))
  }

  def refreshAnalyticsCacheEntry(dataType: String, key: String) = APIAuthAction { req =>
    dataType match {
      case "CampaignDailyCountsReport" => {
        Logger.info(s"manually clearing GA analytics for $key")
        Future{ GoogleAnalytics.getAnalyticsForCampaign(key) }
      }
      case s => Logger.warn(s"manual clear invoked for unexpected data type $dataType")
    }

    NoContent
  }

  def deleteAnalyticsCacheEntry(dataType: String, key: String) = APIAuthAction { req =>
    AnalyticsDataCache.deleteCacheEntry(key, dataType)

    NoContent
  }

}
