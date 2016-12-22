package controllers

import model.reports.{CampaignPageViewsReport, DailyUniqueUsersReport}
import play.api.Logger
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import repositories.{AnalyticsDataCache, CampaignRepository}

import scala.concurrent.Future

class Migration(override val wsClient: WSClient) extends Controller with PandaAuthActions {

  def addCampaignType() = APIAuthAction { req =>
//    val campaigns = CampaignRepository.getAllCampaigns
//
//    campaigns foreach { c =>
//      val cWithType = c.copy(`type` = Some("hosted"))
//      CampaignRepository.putCampaign(cWithType)
//    }

    Ok(s"migration no longer used")
  }

  implicit val ec = AnalyticsDataCache.analyticsExectuionContext

  def buildDailyReports() = APIAuthAction { req =>
    Future {
      val allCampaigns = CampaignRepository.getAllCampaigns()
      val analyticsReports = AnalyticsDataCache.summariseContents

      def reportExists(campaignId: String, reportName: String) = {
        analyticsReports.exists{r => r.dataType == reportName && r.key == campaignId}
      }

      allCampaigns.filter { c =>
        c.startDate.isDefined && c.pathPrefix.isDefined
      }.foreach { c =>
        if (!reportExists(c.id, "CampaignPageViewsReport")) {
          try {CampaignPageViewsReport.getCampaignPageViewsReport(c.id) } catch {case e => Logger.error(s"failed to generate CampaignPageViewsReport for ${c.id}", e)}
        }

        if(!reportExists(c.id, "DailyUniqueUsersReport")) {
          try {DailyUniqueUsersReport.getDailyUniqueUsersReport(c.id) } catch {case e => Logger.error(s"failed to generate CampaignPageViewsReport for ${c.id}", e)}
        }

      }

    }

    Ok(s"build kicked off")

  }

}
