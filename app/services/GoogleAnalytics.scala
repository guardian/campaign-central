package services

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.{HttpRequest, HttpRequestInitializer}
import com.google.api.services.analyticsreporting.v4.model._
import com.google.api.services.analyticsreporting.v4.{AnalyticsReporting, AnalyticsReportingScopes}
import org.joda.time.DateTime
import play.api.Logger

import scala.collection.JavaConversions._

object GoogleAnalytics {

  private val gaClient = initialiseGaClient

  private val datePattern = "yyyy-MM-dd"

  private def startOfRange(startDate: Option[DateTime]): String = {
    startDate.getOrElse(new DateTime().minusMonths(1)).toString(datePattern)
  }

  private def endOfRange(endDate: Option[DateTime]): String = {
    endDate.flatMap { date =>
      if (date.isBeforeNow) Some(date.toString(datePattern)) else None
    } getOrElse "yesterday"
  }

  def loadDailyCounts(gaFilter: String, startDate: DateTime, endDate: Option[DateTime]): GetReportsResponse = {
    Logger.info(s"fetch ga analytics with filter $gaFilter")

    val dateRange = new DateRange().setStartDate(startDate.toString(datePattern)).setEndDate(endOfRange(endDate))

    val pageViewMetric = new Metric().setExpression("ga:pageviews").setAlias("pageviews")
    val uniqueViewMetric = new Metric().setExpression("ga:uniquePageviews").setAlias("uniques")

    val dateDimension = new Dimension().setName("ga:date")
    val pathDimension = new Dimension().setName("ga:pagePath")

    val viewsReportRequest = new ReportRequest()
      .setDateRanges(List(dateRange))
      .setMetrics(List(pageViewMetric, uniqueViewMetric))
      .setDimensions(List(dateDimension, pathDimension))
      .setFiltersExpression(gaFilter)
      .setIncludeEmptyRows(true)
      .setSamplingLevel("LARGE")
      .setViewId(Config().googleAnalyticsViewId)

    val getReportsRequest = new GetReportsRequest().setReportRequests(List(viewsReportRequest))

    gaClient.reports().batchGet(getReportsRequest).execute()
  }

  def loadCtaClicks(trackingCode: String, startDate: DateTime, endDate: Option[DateTime]): Long = {
    Logger.info(s"fetch CTa clicks for cta $trackingCode")

    val dateRange = new DateRange().setStartDate(startDate.toString(datePattern)).setEndDate(endOfRange(endDate))

    val totalEvents = new Metric().setExpression("ga:totalEvents").setAlias("totalEvents")

    val viewsReportRequest = new ReportRequest()
      .setDateRanges(List(dateRange))
      .setMetrics(List(totalEvents))
      .setFiltersExpression(s"ga:eventCategory==Click;ga:eventAction==External;ga:eventLabel==$trackingCode")
      .setIncludeEmptyRows(true)
      .setSamplingLevel("LARGE")
      .setViewId(Config().googleAnalyticsViewId)

    val getReportsRequest = new GetReportsRequest().setReportRequests(List(viewsReportRequest))

    val reportResponse = gaClient.reports().batchGet(getReportsRequest).execute()

    // this report has a single value, so just dive in grabbing the first entry at each level
    val clickCount = for(
      report <- reportResponse.getReports.headOption;
      row <- report.getData.getRows.headOption;
      metrics <- row.getMetrics.headOption;
      value <- metrics.getValues.headOption
    ) yield { value.toLong}

    clickCount.getOrElse(0L)
  }

  def loadSectionUniqueVisitorCount(sectionId: String, startDate: Option[DateTime], endDate: Option[DateTime]): Long = {
    Logger.info(s"fetch unique visitor count for section '$sectionId'")

    val dateRange = new DateRange().setStartDate(startOfRange(startDate)).setEndDate(endOfRange(endDate))

    val viewsReportRequest = new ReportRequest()
                             .setViewId(Config().googleAnalyticsViewId)
                             .setDateRanges(Seq(dateRange))
                             .setMetrics(Seq(new Metric().setExpression("ga:users").setAlias("users")))
                             .setFiltersExpression(s"ga:dimension4==$sectionId")
                             .setSamplingLevel("LARGE")
                             .setIncludeEmptyRows(true)

    val getReportsRequest = new GetReportsRequest().setReportRequests(Seq(viewsReportRequest))

    val reportResponse = GoogleAnalytics.gaClient.reports().batchGet(getReportsRequest).execute()

    val count = for (
      report <- reportResponse.getReports.headOption;
      row <- report.getData.getRows.headOption;
      metrics <- row.getMetrics.headOption;
      value <- metrics.getValues.headOption
    ) yield {
      value.toLong
    }

    count getOrElse 0
  }

  // general GA connection stuff

  private def initialiseGaClient = {

    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val credential = GoogleCredential.fromStream(Config().googleServiceAccountJsonInputStream)
    val scoped = credential.createScoped(List(AnalyticsReportingScopes.ANALYTICS_READONLY))

    new AnalyticsReporting.Builder(
        httpTransport,
        com.google.api.client.googleapis.util.Utils.getDefaultJsonFactory,
        new TimeoutRequestInitializer(scoped))
      .setApplicationName("campaign central")
      .build()
  }

  private class TimeoutRequestInitializer(creds: HttpRequestInitializer) extends HttpRequestInitializer {
    override def initialize(request: HttpRequest): Unit = {
      creds.initialize(request)
      request.setConnectTimeout(3 * 60000) // 3 minutes connect timeout
      request.setReadTimeout(3 * 60000)
    }
  }
}
