package controllers

import model.reports.{CampaignPageViewsReport, CampaignSummary, DailyUniqueUsersReport}
import play.api.Logger
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents
import repositories.{AnalyticsDataCache, CampaignRepository, GoogleAnalytics}
import services.{AWS, Config}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.control.NonFatal

class Migration(
  override val wsClient: WSClient,
  components: ControllerComponents,
  val aws: AWS,
  val config: Config,
  campaignRepository: CampaignRepository,
  analyticsDataCache: AnalyticsDataCache,
  googleAnalytics: GoogleAnalytics
) extends CentralController(components)
  with PandaAuthActions {

  implicit val ec: ExecutionContextExecutor = analyticsDataCache.analyticsExecutionContext

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
      val allCampaigns     = campaignRepository.getAllCampaigns()
      val analyticsReports = analyticsDataCache.summariseContents

      def reportExists(campaignId: String, reportName: String) = {
        analyticsReports.exists { r =>
          r.dataType == reportName && r.key == campaignId
        }
      }

      allCampaigns
        .filter { c =>
          c.startDate.isDefined && c.pathPrefix.isDefined
        }
        .foreach { c =>
          if (!reportExists(c.id, "CampaignPageViewsReport")) {
            try {
              CampaignPageViewsReport
                .getCampaignPageViewsReport(campaignRepository, analyticsDataCache, googleAnalytics, c.id)
            } catch {
              case NonFatal(e) =>
                Logger.error(s"failed to generate CampaignPageViewsReport for ${c.id}", e)
            }
          }

          if (!reportExists(c.id, "DailyUniqueUsersReport")) {
            try {
              DailyUniqueUsersReport
                .getDailyUniqueUsersReport(campaignRepository, analyticsDataCache, googleAnalytics, c.id)
            } catch {
              case NonFatal(e) =>
                Logger.error(s"failed to generate CampaignPageViewsReport for ${c.id}", e)
            }
          }

        }

    }

    Ok("build kicked off")

  }

  def rebuildCampaignSummaries() = APIAuthAction {
    Future {
      val allCampaigns = campaignRepository.getAllCampaigns()

      allCampaigns
        .filter { c =>
          c.startDate.isDefined && c.pathPrefix.isDefined
        }
        .foreach { c =>
          val uuReport = DailyUniqueUsersReport
            .getDailyUniqueUsersReport(campaignRepository, analyticsDataCache, googleAnalytics, c.id)
          uuReport.foreach { r =>
            CampaignSummary.storeLatestUniquesForCampaign(analyticsDataCache, c, r.dailyUniqueUsers.lastOption)
          }
        }
    }

    Ok("build kicked off")
  }

}
