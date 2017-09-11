package controllers

import java.util.concurrent.Executors

import model.command.RefreshCampaignFromCAPICommand
import model.reports._
import model.{TrafficDriverGroupStats, User}
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents
import repositories._
import services.{AWS, Config}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class ManagementApi(
  override val wsClient: WSClient,
  components: ControllerComponents,
  val aws: AWS,
  val config: Config,
  campaignRepository: CampaignRepository,
  campaignContentRepository: CampaignContentRepository,
  analyticsDataCache: AnalyticsDataCache,
  googleAnalytics: GoogleAnalytics,
  contentApi: ContentApi,
  tagManagerApi: TagManagerApi
) extends CentralController(components)
  with HMACPandaAuthActions {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def getAnalyticsCacheSummary() = APIHMACAuthAction { _ =>
    Ok(Json.toJson(analyticsDataCache.summariseContents))
  }

  def refreshAnalyticsCacheEntry(dataType: String, key: String) = APIHMACAuthAction { _ =>
    refreshEntry(dataType, key)
    NoContent
  }

  def deleteAnalyticsCacheEntry(dataType: String, key: String) = APIHMACAuthAction { _ =>
    analyticsDataCache.deleteCacheEntry(key, dataType)
    NoContent
  }

  def refreshAnalyticsCacheForType(dataType: String) = APIHMACAuthAction { _ =>
    analyticsDataCache.summariseContents
      .filter { e =>
        val expired = e.expires.exists(_ < System.currentTimeMillis)
        e.dataType == dataType && expired
      }
      .foreach { e =>
        refreshEntry(e.dataType, e.key)
      }

    NoContent
  }

  private def refreshEntry(dataType: String, key: String) = {
    dataType match {
      case "CtaClicksReport" =>
        Logger.info(s"manually clearing GA CTA CTR analytics for $key")
        Future { CtaClicksReport.getCtaClicksForCampaign(campaignRepository, analyticsDataCache, googleAnalytics, key) }
      case "DailyUniqueUsersReport" =>
        Logger.info(s"manually clearing DailyUniqueUsersReport analytics for $key")
        Future {
          DailyUniqueUsersReport.getDailyUniqueUsersReport(campaignRepository, analyticsDataCache, googleAnalytics, key)
        }
      case "CampaignPageViewsReport" =>
        Logger.info(s"manually clearing CampaignPageViewsReport analytics for $key")
        Future {
          CampaignPageViewsReport.getCampaignPageViewsReport(
            campaignRepository,
            analyticsDataCache,
            googleAnalytics,
            key
          )
        }
      case "TrafficDriverGroupStats" =>
        Logger.info(s"manually clearing Traffic driver stats for $key")
        Future { TrafficDriverGroupStats.forCampaign(config, campaignRepository, analyticsDataCache, key) }
      case "QualifiedPercentagesReport" =>
        Logger.info(s"manually clearing Qualified stats for $key")
        Future {
          QualifiedPercentagesReport.getQualifiedPercentagesReportForCampaign(
            campaignRepository,
            analyticsDataCache,
            googleAnalytics,
            key
          )
        }
      case _ => Logger.warn(s"manual clear invoked for unexpected data type $dataType")
    }
  }

  def refreshExpiringCampaigns = APIHMACAuthAction {

    implicit val user: Option[User] = Some(User("campaign", "refresher", "labs.beta@guardian.co.uk"))

    val expiringCampaigns = campaignRepository.getAllCampaigns().filter { c =>
      c.status == "live" && c.endDate.exists(_.isBeforeNow)
    }

    expiringCampaigns foreach { c =>
      Logger.info(s"campaign ${c.name} is due to expire, refreshing from CAPI")
      RefreshCampaignFromCAPICommand(c.id).process(campaignRepository,
                                                   campaignContentRepository,
                                                   contentApi,
                                                   tagManagerApi)
    }

    NoContent
  }
}
