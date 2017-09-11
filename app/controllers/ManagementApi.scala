package controllers

import java.util.concurrent.Executors

import model.command.RefreshCampaignFromCAPICommand
import model.reports._
import model.{TrafficDriverGroupStats, User}
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{AnalyticsDataCache, CampaignRepository}

import scala.concurrent.{ExecutionContext, Future}

class ManagementApi(override val wsClient: WSClient, components: ControllerComponents)
    extends CentralController(components) with HMACPandaAuthActions {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def getAnalyticsCacheSummary() = APIHMACAuthAction { req =>
    Ok(Json.toJson(AnalyticsDataCache.summariseContents))
  }

  def refreshAnalyticsCacheEntry(dataType: String, key: String) = APIHMACAuthAction { req =>
    refreshEntry(dataType, key)

    NoContent
  }

  def deleteAnalyticsCacheEntry(dataType: String, key: String) = APIHMACAuthAction { req =>
    AnalyticsDataCache.deleteCacheEntry(key, dataType)

    NoContent
  }

  def refreshAnalyticsCacheForType(dataType: String) = APIHMACAuthAction { req =>
    AnalyticsDataCache.summariseContents.filter{ e =>
      val expired = e.expires.exists(_ < System.currentTimeMillis)
      e.dataType == dataType && expired
    }.foreach{ e =>
      refreshEntry(e.dataType, e.key)
    }

    NoContent
  }

  private def refreshEntry(dataType: String, key: String) = {
    dataType match {
      case "CtaClicksReport" => {
        Logger.info(s"manually clearing GA CTA CTR analytics for $key")
        Future {CtaClicksReport.getCtaClicksForCampaign(key)}
      }
      case "DailyUniqueUsersReport" => {
        Logger.info(s"manually clearing DailyUniqueUsersReport analytics for $key")
        Future {DailyUniqueUsersReport.getDailyUniqueUsersReport(key)}
      }
      case "CampaignPageViewsReport" => {
        Logger.info(s"manually clearing CampaignPageViewsReport analytics for $key")
        Future {CampaignPageViewsReport.getCampaignPageViewsReport(key)}
      }
      case "TrafficDriverGroupStats" => {
        Logger.info(s"manually clearing Traffic driver stats for $key")
        Future {TrafficDriverGroupStats.forCampaign(key)}
      }
      case "QualifiedPercentagesReport" => {
        Logger.info(s"manually clearing Qualified stats for $key")
        Future {QualifiedPercentagesReport.getQualifiedPercentagesReportForCampaign(key)}
      }
      case s => Logger.warn(s"manual clear invoked for unexpected data type $dataType")
    }
  }

  def refreshExpiringCampaigns = APIHMACAuthAction {

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
