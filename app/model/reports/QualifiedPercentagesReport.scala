package model.reports

import ai.x.play.json.Jsonx
import model.Campaign
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Format
import repositories._

import scala.concurrent.{ExecutionContextExecutor, Future}

case class QualifiedMetricReport(total: Long, qualifiedCount: Long, percentage: Double)

object QualifiedMetricReport {
  implicit val qualifiedMetricReportFormat: Format[QualifiedMetricReport] = Jsonx.formatCaseClass[QualifiedMetricReport]
}

case class QualifiedPercentagesReport(campaignId: String, metrics: Map[String, QualifiedMetricReport]) {

  def refresh(
    campaignRepository: CampaignRepository,
    analyticsDataCache: AnalyticsDataCache,
    googleAnalytics: GoogleAnalytics
  ): Option[QualifiedPercentagesReport] =
    QualifiedPercentagesReport.generateReport(campaignRepository, analyticsDataCache, googleAnalytics, campaignId)

}

object QualifiedPercentagesReport {

  implicit val qualifiedPercentagesReportFormat: Format[QualifiedPercentagesReport] =
    Jsonx.formatCaseClass[QualifiedPercentagesReport]

  def getQualifiedPercentagesReportForCampaign(campaignRepository: CampaignRepository,
                                               analyticsDataCache: AnalyticsDataCache,
                                               googleAnalytics: GoogleAnalytics,
                                               campaignId: String): Option[QualifiedPercentagesReport] = {
    implicit val ec: ExecutionContextExecutor = analyticsDataCache.analyticsExecutionContext

    analyticsDataCache.getCampaignQualifiedPercentagesReport(campaignId) match {
      case Hit(report) =>
        Logger.debug(s"getting qualified percentages for campaign $campaignId - cache hit")
        Some(report)
      case Stale(report) =>
        Logger.debug(s"getting qualified percentages for campaign $campaignId - cache stale spawning async refresh")

        Future {
          Logger.debug(s"async refresh of qualified percentages for campaign $campaignId")
          report.refresh(campaignRepository, analyticsDataCache, googleAnalytics)
        } // serve stale but spawn refresh future
        Some(report)
      case Miss =>
        Logger.debug(s"getting qualified percentages for campaign $campaignId - cache miss fetching sync")

        generateReport(campaignRepository, analyticsDataCache, googleAnalytics, campaignId)
    }
  }

  val qualifiedMetricFetchers = List(
    ContentTypeDwellTimeMetric("article", 15),
    ContentTypeDwellTimeMetric("gallery", 15),
    ContentTypeDwellTimeMetric("interactive", 15),
    VideoCompletionMetricFetcher()
  )

  def generateReport(campaignRepository: CampaignRepository,
                     analyticsDataCache: AnalyticsDataCache,
                     googleAnalytics: GoogleAnalytics,
                     campaignId: String): Option[QualifiedPercentagesReport] = {
    campaignRepository.getCampaign(campaignId) flatMap { campaign =>
      for (startDate <- campaign.startDate) yield {

        val reportLines = qualifiedMetricFetchers.foldLeft(Map[String, QualifiedMetricReport]()) { (m, fetcher) =>
          m ++ fetcher.fetch(googleAnalytics, campaign, startDate, campaign.endDate)
        }

        val report = QualifiedPercentagesReport(campaignId, reportLines)

        analyticsDataCache.putQualifiedPercentagesReport(
          campaignId,
          report,
          analyticsDataCache.calculateValidToDateForDailyStats(campaign)
        )

        report
      }
    }
  }
}

sealed trait QualifiedMetricReportFetcher {
  def fetch(
    googleAnalytics: GoogleAnalytics,
    campaign: Campaign,
    startDate: DateTime,
    endDate: Option[DateTime]
  ): Map[String, QualifiedMetricReport]
}

case class ContentTypeDwellTimeMetric(contentType: String, qualifiedDwellTime: Int)
  extends QualifiedMetricReportFetcher {

  override def fetch(
    googleAnalytics: GoogleAnalytics,
    campaign: Campaign,
    startDate: DateTime,
    endDate: Option[DateTime]
  ): Map[String, QualifiedMetricReport] = {

    val campaignFilter = if (campaign.`type` == "hosted") {
      campaign.gaFilterExpression
    } else {
      campaign.pathPrefix.map { section =>
        s"ga:dimension4==$section"
      }
    }

    val report = campaignFilter.map { filter =>
      val totalHits = googleAnalytics.loadTotalCampaignContentTypeViews(filter, contentType, startDate, endDate)
      if (totalHits > 0) {
        val qualifiedHits = googleAnalytics.loadCampaignContentTypeViewsWithDwellTime(
          filter,
          contentType,
          qualifiedDwellTime,
          startDate,
          endDate
        )
        Map(
          s"${contentType}DwellTime" -> QualifiedMetricReport(
            totalHits,
            qualifiedHits,
            (qualifiedHits.toDouble / totalHits) * 100
          ))
      } else {
        Map[String, QualifiedMetricReport]()
      }
    }

    report.getOrElse(Map())
  }
}

case class VideoCompletionMetricFetcher() extends QualifiedMetricReportFetcher {
  override def fetch(
    googleAnalytics: GoogleAnalytics,
    campaign: Campaign,
    startDate: DateTime,
    endDate: Option[DateTime]
  ): Map[String, QualifiedMetricReport] = {
    val campaignFilter = if (campaign.`type` == "hosted") {
      campaign.gaFilterExpression
    } else {
      campaign.pathPrefix.map { section =>
        s"ga:dimension4==$section"
      }
    }

    val report = campaignFilter.map { filter =>
      val totalHits = googleAnalytics.loadTotalCampaignContentTypeViews(filter, "video", startDate, endDate)
      if (totalHits > 0) {
        val completionCounts = googleAnalytics.loadVideoCompletionCounts(filter, startDate, endDate)

        Map(
          "videoPlays" -> QualifiedMetricReport(totalHits,
                                                completionCounts.playMedia,
                                                (completionCounts.playMedia.toDouble / totalHits) * 100),
          "video25Percent" -> QualifiedMetricReport(totalHits,
                                                    completionCounts.media25Complete,
                                                    (completionCounts.media25Complete.toDouble / totalHits) * 100),
          "video50Percent" -> QualifiedMetricReport(totalHits,
                                                    completionCounts.media50Complete,
                                                    (completionCounts.media50Complete.toDouble / totalHits) * 100),
          "video75Percent" -> QualifiedMetricReport(totalHits,
                                                    completionCounts.media75Complete,
                                                    (completionCounts.media75Complete.toDouble / totalHits) * 100),
          "videoComplete" -> QualifiedMetricReport(totalHits,
                                                   completionCounts.media100Complete,
                                                   (completionCounts.media100Complete.toDouble / totalHits) * 100)
        )
      } else {
        Map[String, QualifiedMetricReport]()
      }
    }
    report.getOrElse(Map())
  }
}
