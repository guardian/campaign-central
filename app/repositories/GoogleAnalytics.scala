package repositories

import java.util.concurrent.Executors

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.{HttpRequest, HttpRequestInitializer}
import com.google.api.services.analyticsreporting.v4.model._
import com.google.api.services.analyticsreporting.v4.{AnalyticsReporting, AnalyticsReportingScopes}
import model.Campaign
import org.joda.time.{DateTime, Duration}
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import services.Config

import scala.collection.JavaConversions._

object GoogleAnalytics {

  val gaClient = initialiseGaClient

  // page views
  case class DailyViewCounts(seenPaths: Set[String], countStats: Map[String, Long])

  def loadPageViewsForDay(gaFilter: String, date: DateTime): Option[DailyViewCounts] = {
    Logger.info(s"fetch pageView analytics with filter ${gaFilter} for day ${date.toString("yyyy-MM-dd")}")

    val dateRange = new DateRange().setStartDate(date.toString("yyyy-MM-dd")).setEndDate(date.toString("yyyy-MM-dd"))

    val pageViewMetric = new Metric().setExpression("ga:pageviews").setAlias("pageViews")
    val uniqueViewMetric = new Metric().setExpression("ga:uniquePageviews").setAlias("uniquePageViews")

    val dateDimension = new Dimension().setName("ga:date")
    val pathDimension = new Dimension().setName("ga:pageTitle")

    val viewsReportRequest = new ReportRequest()
      .setDateRanges(List(dateRange))
      .setMetrics(List(pageViewMetric, uniqueViewMetric))
      .setDimensions(List(dateDimension, pathDimension))
      .setFiltersExpression(gaFilter)
      .setIncludeEmptyRows(true)
      .setSamplingLevel("LARGE")
      .setViewId(getViewIdForReport("pageViews", Some(date)))

    val getReportsRequest = new GetReportsRequest().setReportRequests(List(viewsReportRequest))

    val reportResponse = gaClient.reports().batchGet(getReportsRequest).execute()

    reportResponse.getReports.foreach{ report => warnIfDataIsSampled(report, s"pageView analytics with filter ${gaFilter} for day ${date.toString("yyyy-MM-dd")}")}

    parseDailyViewCountsReport(reportResponse)
  }

  private def parseDailyViewCountsReport(reportResponse: GetReportsResponse): Option[DailyViewCounts] = {
    for (
      report <- reportResponse.getReports.headOption;
      rows <- Option(report.getData.getRows)
    ) yield {
      val header = report.getColumnHeader
      val dimensions = header.getDimensions

      val pathDimIndex = dimensions.indexOf("ga:pageTitle")

      val metricHeaders = header.getMetricHeader.getMetricHeaderEntries

      val pageviewsIndex = metricHeaders.indexWhere(_.getName == "pageViews")
      val uniquesIndex = metricHeaders.indexWhere(_.getName == "uniquePageViews")

      var seenPaths: Set[String] = Set()
      var countStats: Map[String, Long] = Map()

      for (
        row <- rows.toList;
        dateRangeValues <- row.getMetrics.headOption
      ) {
        val path = row.getDimensions.apply(pathDimIndex)

        val pageviews = dateRangeValues.getValues.apply(pageviewsIndex).toLong
        val uniques = dateRangeValues.getValues.apply(uniquesIndex).toLong

        seenPaths = seenPaths + path
        countStats = countStats ++ Map(s"count$path" -> pageviews, s"unique$path" -> uniques)
      }

      DailyViewCounts(seenPaths, countStats)
    }
  }

  // Daily unique users
  def loadUniqueUsersDay(gaFilter: String, date: DateTime): Long = {
    Logger.info(s"fetch daily user analytics with filter ${gaFilter} for day ${date.toString("yyyy-MM-dd")}")

    val dateRange = new DateRange().setStartDate(date.toString("yyyy-MM-dd")).setEndDate(date.toString("yyyy-MM-dd"))

    val userMetric = new Metric().setExpression("ga:users").setAlias("users")

    val dailyUniquesRequest = new ReportRequest()
      .setDateRanges(List(dateRange))
      .setMetrics(List(userMetric))
      .setFiltersExpression(gaFilter)
      .setIncludeEmptyRows(true)
      .setSamplingLevel("LARGE")
      .setViewId(getViewIdForReport("dailyUniqueUsers", Some(date)))

    val getReportsRequest = new GetReportsRequest().setReportRequests(List(dailyUniquesRequest))

    val reportResponse = gaClient.reports().batchGet(getReportsRequest).execute()

    reportResponse.getReports.foreach{ report => warnIfDataIsSampled(report, s"fetch daily user analytics with filter ${gaFilter} for day ${date.toString("yyyy-MM-dd")}")}

    parseDimensionlessSingleMetricReport(reportResponse)
  }


  // CTA clicks functions

  def loadCtaClicks(trackingCode: String, startDate: DateTime, endDate: Option[DateTime]): Long = {

    Logger.info(s"fetch CTA clicks for cta $trackingCode")

    val endOfRange = endDate.flatMap{ed => if(ed.isBeforeNow) Some(ed.toString("yyyy-MM-dd")) else None}.getOrElse("yesterday")
    val dateRange = new DateRange().setStartDate(startDate.toString("yyyy-MM-dd")).setEndDate(endOfRange)

    val totalEvents = new Metric().setExpression("ga:totalEvents").setAlias("totalEvents")

    val viewsReportRequest = new ReportRequest()
      .setDateRanges(List(dateRange))
      .setMetrics(List(totalEvents))
      .setFiltersExpression(s"ga:eventCategory==Click;ga:eventAction==External;ga:eventLabel==$trackingCode")
      .setIncludeEmptyRows(true)
      .setSamplingLevel("LARGE")
      .setViewId(getViewIdForReport("ctaCtr"))

    val getReportsRequest = new GetReportsRequest().setReportRequests(List(viewsReportRequest))

    val reportResponse = gaClient.reports().batchGet(getReportsRequest).execute()

    reportResponse.getReports.foreach{ report => warnIfDataIsSampled(report, s"CTA clicks for cta $trackingCode")}
    // this report has a single value, so just dive in grabbing the first entry at each level
    parseDimensionlessSingleMetricReport(reportResponse)
  }

  // qualified reporting functions

  def loadTotalCampaignContentTypeViews(campaignFilter: String, contentType: String, startDate: DateTime, endDate: Option[DateTime]): Long = {
    loadTotalViewsWithFilter(s"$campaignFilter;ga:dimension5==$contentType", startDate, endDate)
  }

  def loadCampaignContentTypeViewsWithDwellTime(campaignFilter: String, contentType: String, qualifiedDwellTime: Int, startDate: DateTime, endDate: Option[DateTime]): Long = {
    loadTotalViewsWithFilter(s"$campaignFilter;ga:dimension5==$contentType;ga:timeOnPage>=$qualifiedDwellTime", startDate, endDate)
  }

  private def loadTotalViewsWithFilter(filter: String, startDate: DateTime, endDate: Option[DateTime]): Long = {
    Logger.info(s"fetch pageViews by filter $filter")

    val endOfRange = endDate.flatMap{ed => if(ed.isBeforeNow) Some(ed.toString("yyyy-MM-dd")) else None}.getOrElse("yesterday")
    val dateRange = new DateRange().setStartDate(startDate.toString("yyyy-MM-dd")).setEndDate(endOfRange)

    val pageViews = new Metric().setExpression("ga:pageViews").setAlias("pageViews")

    val viewsReportRequest = new ReportRequest()
      .setDateRanges(List(dateRange))
      .setMetrics(List(pageViews))
      .setFiltersExpression(filter)
      .setIncludeEmptyRows(true)
      .setSamplingLevel("LARGE")
      .setViewId(getViewIdForReport("totalPageViewsByFilter"))

    val getReportsRequest = new GetReportsRequest().setReportRequests(List(viewsReportRequest))

    val reportResponse = gaClient.reports().batchGet(getReportsRequest).execute()

    reportResponse.getReports.foreach{ report => warnIfDataIsSampled(report, s"pageViews by filter $filter")}
    // this report has a single value, so just dive in grabbing the first entry at each level
    parseDimensionlessSingleMetricReport(reportResponse)
  }

  // general GA stuff

  private def parseDimensionlessSingleMetricReport(reportResponse: GetReportsResponse): Long = {
    // this report has a single value, so just dive in grabbing the first entry at each level
    val clickCount = for(
      report <- reportResponse.getReports.headOption;
      data <- Option(report.getData);
      rows <- Option(data.getRows);
      row <- rows.headOption;
      metrics <- row.getMetrics.headOption;
      value <- metrics.getValues.headOption
    ) yield { value.toLong}

    clickCount.getOrElse(0L)
  }

  private def warnIfDataIsSampled(report: Report, reportTitle: String): Unit = {
    Option(report.getData.getSamplesReadCounts).foreach{ readCounts =>
      val readcount = readCounts.headOption.getOrElse(0L)
      val sampleSpace = Option(report.getData.getSamplingSpaceSizes).map(_.headOption.getOrElse(0L)).getOrElse(0L)

      Logger.warn(s"warning $reportTitle is sampled. $readcount / $sampleSpace")

    }
  }

  private val GLABS_VIEW_START_DATE = new DateTime("2016-12-11")

  private def getViewIdForReport(reportType: String, date: Option[DateTime] = None): String = {
    def useGlabsAccountWhenAvailable = {
      if (date.exists(_.isAfter(GLABS_VIEW_START_DATE)))
        Config().googleAnalyticsGlabsViewId
      else
        Config().googleAnalyticsViewId
    }

    reportType match {
      case "ctaCtr" => Config().googleAnalyticsViewId
      case "pageViews" => useGlabsAccountWhenAvailable
      case "dailyUniqueUsers" => useGlabsAccountWhenAvailable
      case "totalPageViewsByFilter" => Config().googleAnalyticsGlabsViewId
      case _ => useGlabsAccountWhenAvailable
    }
  }

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
      creds.initialize(request);
      request.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
      request.setReadTimeout(3 * 60000);
    }
  }
}
