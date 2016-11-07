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
    refreshEntry(dataType, key)

    NoContent
  }

  def deleteAnalyticsCacheEntry(dataType: String, key: String) = APIAuthAction { req =>
    AnalyticsDataCache.deleteCacheEntry(key, dataType)

    NoContent
  }

  def refreshAnalyticsCacheForType(dataType: String) = APIAuthAction { req =>
    AnalyticsDataCache.summariseContents.filter{ e =>
      val expired = e.expires.map(_ < System.currentTimeMillis).getOrElse(false)
      e.dataType == dataType && expired
    }.foreach{ e =>
      refreshEntry(e.dataType, e.key)
    }

    NoContent
  }

  private def refreshEntry(dataType: String, key: String) = {
    dataType match {
      case "CampaignDailyCountsReport" => {
        Logger.info(s"manually clearing GA analytics for $key")
        Future {GoogleAnalytics.getAnalyticsForCampaign(key)}
      }
      case s => Logger.warn(s"manual clear invoked for unexpected data type $dataType")
    }
  }
}
