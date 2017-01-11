package model.reports

import ai.x.play.json.Jsonx
import play.api.Logger
import play.api.libs.json.Format
import repositories._

import scala.concurrent.Future

// the data is CTA atom id -> number of clicks
case class CtaClicksReport(campaignId: String, data: Map[String, Long]) {

  def refresh = CtaClicksReport.generateReport(campaignId)
}

object CtaClicksReport {

  implicit val ec = AnalyticsDataCache.analyticsExectuionContext

  implicit val actaClicksReportyFormat: Format[CtaClicksReport] = Jsonx.formatCaseClass[CtaClicksReport]

  def getCtaClicksForCampaign(campaignId: String): Option[CtaClicksReport] = {

    AnalyticsDataCache.getCampaignCtaClicksReport(campaignId) match {
      case Hit(report) => {
        Logger.debug(s"getting CTA clicks for campaign $campaignId - cache hit")
        Some(report)
      }
      case Stale(report) => {
        Logger.debug(s"getting CTA clicks for campaign $campaignId - cache stale spawning async refresh")

        Future{
          Logger.debug(s"async refresh of CTA clicks for campaign $campaignId")
          report.refresh
        } // serve stale but spawn refresh future
        Some(report)
      }
      case Miss => {
        Logger.debug(s"getting CTA clicks for campaign $campaignId - cache miss fetching sync")

        generateReport(campaignId)
      }
    }
  }

  def generateReport(campaignId: String): Option[CtaClicksReport] = {
    CampaignRepository.getCampaign(campaignId) map { campaign =>
      val ctaReportLines = for (
        cta <- campaign.callToActions;
        startDate <- campaign.startDate;
        builderId <- cta.builderId;
        trackingCode <- cta.trackingCode
      ) yield {
        builderId -> GoogleAnalytics.loadCtaClicks(trackingCode, startDate, campaign.endDate)
      }

      val logoReportLines = for (
        startDate <- campaign.startDate;
        sectionId <- campaign.pathPrefix
      ) yield {
        campaign.pathPrefix
        "logo" -> GoogleAnalytics.loadSponsorLogoClicks(sectionId, startDate, campaign.endDate)
      }

      val report = CtaClicksReport(campaignId, (logoReportLines.toList ::: ctaReportLines).toMap)

      AnalyticsDataCache.putCampaignCtaClicksReport(campaignId, report, AnalyticsDataCache.calculateValidToDateForDailyStats(campaign))

      report
    }
  }
}
