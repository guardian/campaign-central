package controllers

import model.reports.{CampaignPageViewsReport, CampaignSummary, DailyUniqueUsersReport}
import play.api.Logger
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents, ControllerHelpers, PlayBodyParsers}
import repositories.{AnalyticsDataCache, CampaignRepository}

import scala.concurrent.Future
import scala.util.control.NonFatal

class Migration(override val wsClient: WSClient, components: ControllerComponents)
  extends CentralController(components) with PandaAuthActions {

  implicit val ec = AnalyticsDataCache.analyticsExecutionContext

  def addCampaignType() = APIAuthAction {
//    val campaigns = CampaignRepository.getAllCampaigns
//
//    campaigns foreach { c =>
//      val cWithType = c.copy(`type` = Some("hosted"))
//      CampaignRepository.putCampaign(cWithType)
//    }

    Ok("migration no longer used")
  }

  def buildDailyReports() = APIAuthAction {
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
          try { CampaignPageViewsReport.getCampaignPageViewsReport(c.id) } catch { case NonFatal(e) =>
            Logger.error(s"failed to generate CampaignPageViewsReport for ${c.id}", e)
          }
        }

        if(!reportExists(c.id, "DailyUniqueUsersReport")) {
          try { DailyUniqueUsersReport.getDailyUniqueUsersReport(c.id) } catch { case NonFatal(e) =>
            Logger.error(s"failed to generate CampaignPageViewsReport for ${c.id}", e)
          }
        }

      }

    }

    Ok("build kicked off")

  }

  def rebuildCampaignSummaries() = APIAuthAction {
    Future {
      val allCampaigns = CampaignRepository.getAllCampaigns()

      allCampaigns.filter { c =>
        c.startDate.isDefined && c.pathPrefix.isDefined
      }.foreach { c =>
        val uuReport = DailyUniqueUsersReport.getDailyUniqueUsersReport(c.id)
        uuReport.foreach{ r => CampaignSummary.storeLatestUniquesForCampaign(c, r.dailyUniqueUsers.lastOption) }
      }
    }

    Ok("build kicked off")
  }

}
