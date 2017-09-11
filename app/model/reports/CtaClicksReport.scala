package model.reports

import ai.x.play.json.Jsonx
import play.api.Logger
import play.api.libs.json.Format
import repositories._

import scala.concurrent.{ExecutionContext, Future}

// the data is CTA atom id -> number of clicks
case class CtaClicksReport(campaignId: String, data: Map[String, Long]) {

  def refresh(
    campaignRepository: CampaignRepository,
    analyticsDataCache: AnalyticsDataCache,
    googleAnalytics: GoogleAnalytics
  ): Option[CtaClicksReport] =
    CtaClicksReport.generateReport(campaignRepository, analyticsDataCache, googleAnalytics, campaignId)
}

object CtaClicksReport {

  implicit val actaClicksReportyFormat: Format[CtaClicksReport] = Jsonx.formatCaseClass[CtaClicksReport]

  def getCtaClicksForCampaign(
    campaignRepository: CampaignRepository,
    analyticsDataCache: AnalyticsDataCache,
    googleAnalytics: GoogleAnalytics,
    campaignId: String
  ): Option[CtaClicksReport] = {
    implicit val ec: ExecutionContext = analyticsDataCache.analyticsExecutionContext

    analyticsDataCache.getCampaignCtaClicksReport(campaignId) match {
      case Hit(report) =>
        Logger.debug(s"getting CTA clicks for campaign $campaignId - cache hit")
        Some(report)
      case Stale(report) =>
        Logger.debug(s"getting CTA clicks for campaign $campaignId - cache stale spawning async refresh")

        Future {
          Logger.debug(s"async refresh of CTA clicks for campaign $campaignId")
          report.refresh(campaignRepository, analyticsDataCache, googleAnalytics)
        } // serve stale but spawn refresh future
        Some(report)
      case Miss =>
        Logger.debug(s"getting CTA clicks for campaign $campaignId - cache miss fetching sync")

        generateReport(campaignRepository, analyticsDataCache, googleAnalytics, campaignId)
    }
  }

  def generateReport(
    campaignRepository: CampaignRepository,
    analyticsDataCache: AnalyticsDataCache,
    googleAnalytics: GoogleAnalytics,
    campaignId: String
  ): Option[CtaClicksReport] = {
    campaignRepository.getCampaign(campaignId) map { campaign =>
      val ctaReportLines = for (cta <- campaign.callToActions;
                                startDate    <- campaign.startDate;
                                builderId    <- cta.builderId;
                                trackingCode <- cta.trackingCode) yield {
        builderId -> googleAnalytics.loadCtaClicks(trackingCode, startDate, campaign.endDate)
      }

      val logoReportLines = for (startDate <- campaign.startDate;
                                 sectionId <- campaign.pathPrefix) yield {
        "logo" -> googleAnalytics.loadSponsorLogoClicks(sectionId, startDate, campaign.endDate)
      }

      val report = CtaClicksReport(campaignId, (logoReportLines.toList ::: ctaReportLines).toMap)

      analyticsDataCache.putCampaignCtaClicksReport(campaignId,
                                                    report,
                                                    analyticsDataCache.calculateValidToDateForDailyStats(campaign))

      report
    }
  }
}
