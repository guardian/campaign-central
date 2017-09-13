package controllers

import java.util.concurrent.Executors

import com.gu.googleauth.AuthAction
import model.User
import model.command.RefreshCampaignFromCAPICommand
import model.reports._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
import repositories.{AnalyticsDataCache, CampaignRepository}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class ManagementApi(components: ControllerComponents, authAction: AuthAction[AnyContent])
  extends AbstractController(components) {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def getAnalyticsCacheSummary() = authAction { _ =>
    Ok(Json.toJson(AnalyticsDataCache.summariseContents))
  }

  def refreshAnalyticsCacheEntry(dataType: String, key: String) = authAction { _ =>
    refreshEntry(dataType, key)

    NoContent
  }

  def deleteAnalyticsCacheEntry(dataType: String, key: String) = authAction { _ =>
    AnalyticsDataCache.deleteCacheEntry(key, dataType)

    NoContent
  }

  def refreshAnalyticsCacheForType(dataType: String) = authAction { _ =>
    AnalyticsDataCache.summariseContents
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
        Future { CtaClicksReport.getCtaClicksForCampaign(key) }
      case "DailyUniqueUsersReport" =>
        Logger.info(s"manually clearing DailyUniqueUsersReport analytics for $key")
        Future { DailyUniqueUsersReport.getDailyUniqueUsersReport(key) }
      case "CampaignPageViewsReport" =>
        Logger.info(s"manually clearing CampaignPageViewsReport analytics for $key")
        Future { CampaignPageViewsReport.getCampaignPageViewsReport(key) }
      case "QualifiedPercentagesReport" =>
        Logger.info(s"manually clearing Qualified stats for $key")
        Future { QualifiedPercentagesReport.getQualifiedPercentagesReportForCampaign(key) }
      case _ => Logger.warn(s"manual clear invoked for unexpected data type $dataType")
    }
  }

  def refreshExpiringCampaigns = authAction {

    implicit val user: User = User("campaign", "refresher", "labs.beta@guardian.co.uk")

    val expiringCampaigns = CampaignRepository.getAllCampaigns().filter { c =>
      c.status == "live" && c.endDate.exists(_.isBeforeNow)
    }

    expiringCampaigns foreach { c =>
      Logger.info(s"campaign ${c.name} is due to expire, refreshing from CAPI")
      RefreshCampaignFromCAPICommand(c.id).process()
    }

    NoContent
  }
}
