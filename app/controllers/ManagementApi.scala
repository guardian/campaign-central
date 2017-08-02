package controllers

import java.util.concurrent.Executors

import model.User
import model.command.RefreshCampaignFromCAPICommand
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import repositories.{AnalyticsDataCache, CampaignRepository}

import scala.concurrent.ExecutionContext

class ManagementApi(override val wsClient: WSClient) extends Controller with HMACPandaAuthActions {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def getAnalyticsCacheSummary() = APIHMACAuthAction { req =>
    Ok(Json.toJson(AnalyticsDataCache.summariseContents))
  }


  def deleteAnalyticsCacheEntry(dataType: String, key: String) = APIHMACAuthAction { req =>
    AnalyticsDataCache.deleteCacheEntry(key, dataType)

    NoContent
  }

  def refreshExpiringCampaigns = APIHMACAuthAction { req =>

    implicit val user = User("campaign", "refresher", "labs.beta@guardian.co.uk")

    val expiringCampaigns = CampaignRepository.getAllCampaigns().filter{ c =>
      c.status == "live" && c.endDate.exists(_.isBeforeNow)
    }

    expiringCampaigns foreach { c =>
      Logger.info(s"campaign ${c.name} is due to expire, refreshing from CAPI")
      RefreshCampaignFromCAPICommand(c.id).process()
    }

    NoContent
  }
}
