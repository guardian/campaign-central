package repositories

import java.util.concurrent.Executors

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.{HttpRequest, HttpRequestInitializer}
import com.google.api.services.analyticsreporting.v4.model._
import com.google.api.services.analyticsreporting.v4.{AnalyticsReporting, AnalyticsReportingScopes}
import model.{Campaign, CampaignDailyCountsReport}
import org.joda.time.{DateTime, Duration}
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import services.Config

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}




object GoogleAnalytics {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  val gaClient = initialiseGaClient

  def getAnalyticsForCampaign(campaignId: String): Option[CampaignDailyCountsReport] = {

    AnalyticsDataCache.getCampaignDailyCountsReport(campaignId) match {
      case Hit(report) => {
        Logger.debug(s"getting analytics for campaign $campaignId - cache hit")
        Some(report)
      }
      case Stale(report) => {
        Logger.debug(s"getting analytics for campaign $campaignId - cache stale spawning async refresh")

        Future{
          Logger.debug(s"async refresh of analytics for campaign $campaignId")
          fetchAndStoreAnalyticsForCampaign(campaignId)
        } // serve stale but spawn refresh future
        Some(report)
      }
      case Miss => {
        Logger.debug(s"getting analytics for campaign $campaignId - cache miss fetching sync")
        fetchAndStoreAnalyticsForCampaign(campaignId)
      }
    }
  }

  private def fetchAndStoreAnalyticsForCampaign(campaignId: String): Option[CampaignDailyCountsReport] = {
    val campaign = CampaignRepository.getCampaign(campaignId)

    val report = campaign match {
      case Some(c) if c.`type` == "hosted" => loadAnalyticsForHostedCampaign(c)
      case Some(c) => loadAnalyticsForCampaignContent(c)
      case None => {
        Logger.warn("could not provide analytics for missing campaign")
        None
      }
    }

    report.foreach{ data =>

      val expiry = campaign.flatMap(AnalyticsDataCache.calculateValidToDateForDailyStats)

      AnalyticsDataCache.putCampaignDailyCountsReport(campaignId, data, expiry)
    }

    report
  }

  private def cleanAndConvertRawDailyCounts(rawDailyCounts: ParsedDailyCountsReport, dailyUniqueTargetValue: Option[Long]): CampaignDailyCountsReport = {
    val cleaned = rawDailyCounts
      .zeroMissingPaths
      .calulateDailyTotals
      .addDailyTargets(dailyUniqueTargetValue)
      .calulateCumalativeVales

    CampaignDailyCountsReport(cleaned)
  }

  private def loadAnalyticsForHostedCampaign(campaign: Campaign): Option[CampaignDailyCountsReport] = {
    val dailyCounts = for(
      startDate <- campaign.startDate orElse Some(new DateTime().minusMonths(1) );//campaign.startDate;
      gaFilter <- campaign.gaFilterExpression
    ) yield {
      loadDailyCounts(gaFilter, startDate, campaign.endDate)
    }

    dailyCounts.flatten.map{raw => cleanAndConvertRawDailyCounts(raw, calculateDailyUniqueTarget(campaign))}
  }

  private def loadAnalyticsForCampaignContent(campaign: Campaign): Option[CampaignDailyCountsReport] = {
    Logger.info(s"loading analytics for campaign ${campaign.name} by content items")
    val contentDailyCounts = for(
      content <- CampaignContentRepository.getContentForCampaign(campaign.id).filter(_.isLive);
      startDate <- campaign.startDate;
      path <- content.path
    ) yield {
      val gaFilter = s"ga:pagePath==/$path"
      loadDailyCounts(gaFilter, startDate, campaign.endDate)
    }

    val populatedDailyCounts = contentDailyCounts.flatten
    val dailyCounts = populatedDailyCounts.reduce( (a: ParsedDailyCountsReport, b: ParsedDailyCountsReport) => {

      val allDays = a.dayStats.keySet ++ b.dayStats.keySet
      val combinedDayStats = allDays.map{ day =>
        val ads = a.dayStats.getOrElse(day, Map())
        val bds = b.dayStats.getOrElse(day, Map())

        val statKeys = ads.keySet ++ bds.keySet

        val combinedStats = statKeys.map{ stat =>
          (stat -> (ads.getOrElse(stat, 0L) + bds.getOrElse(stat, 0L)))
        }.toMap

        (day -> combinedStats)
      }.toMap

      ParsedDailyCountsReport(a.seenPaths ++ b.seenPaths, combinedDayStats)
    })

    if(dailyCounts.isEmpty()) None else Some(cleanAndConvertRawDailyCounts(dailyCounts, calculateDailyUniqueTarget(campaign)))
  }


  private def loadDailyCounts(gaFilter: String, startDate: DateTime, endDate: Option[DateTime]): Option[ParsedDailyCountsReport] = {
    Logger.info(s"fetch ga analytics with filter ${gaFilter}")

    val endOfRange = endDate.flatMap{ed => if(ed.isBeforeNow) Some(ed.toString("yyyy-MM-dd")) else None}.getOrElse("yesterday")
    val dateRange = new DateRange().setStartDate(startDate.toString("yyyy-MM-dd")).setEndDate(endOfRange)

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

    val reportResponse = gaClient.reports().batchGet(getReportsRequest).execute()

    parseDailyCountsReport(reportResponse)

  }

  private def calculateDailyUniqueTarget(campaign: Campaign): Option[Long] = {
    for(
      startDate <- campaign.startDate;
      endDate <- campaign.endDate;
      target <- campaign.targets.get("uniques")
    ) yield {
      val days = new Duration(startDate, endDate).getStandardDays
      target / days
    }
  }

  case class ParsedDailyCountsReport(seenPaths: Set[String], dayStats: Map[DateTime, Map[String, Long]]) {

    def isEmpty(): Boolean = {
      seenPaths.isEmpty || dayStats.isEmpty
    }

    def zeroMissingPaths = {
      val zeroMap = seenPaths.flatMap{p => List(s"count$p" -> 0L, s"unique$p" -> 0L)}.toMap
      val zerodDayStats = dayStats.mapValues( zeroMap ++ _)

      ParsedDailyCountsReport(seenPaths, zerodDayStats)
    }

    def calulateDailyTotals = {
      val totalisedDayStats = dayStats.map{ case(dt, dayStat) =>

        val stats = dayStat.keySet

        val totalCount = stats.filter(_.startsWith("count")).foldLeft(0L){case(total, k) => total + dayStat(k)}
        val totalUnique = stats.filter(_.startsWith("unique")).foldLeft(0L){case(total, k) => total + dayStat(k)}

          dt -> (dayStat ++ Map("count-total" -> totalCount, "unique-total" -> totalUnique))
      }

      ParsedDailyCountsReport(seenPaths, totalisedDayStats)
    }

    def addDailyTargets(dailyTarget: Option[Long]) = {

      val dayStatsWithTarget = dailyTarget.map{ target =>
        dayStats.mapValues{stats => stats + ("target-uniques" -> target)}
      }.getOrElse(dayStats)

      ParsedDailyCountsReport(seenPaths, dayStatsWithTarget)
    }

    def calulateCumalativeVales = {
      val days = dayStats.keySet.toList.sortBy(_.getMillis)

      var cumulDayStats = Map.empty[DateTime, Map[String, Long]]

      days.zipWithIndex.foreach{ case(dt, idx) =>
        val dayStat = dayStats(dt)
        val dayKeys = dayStat.keySet

        idx match {
          case 0 => {
            val totals = dayKeys.map{k => s"cumulative-$k" -> dayStat(k)}.toMap
            cumulDayStats = cumulDayStats + (dt -> (totals ++ dayStat))
          }
          case i => {
            val prevDayStats = cumulDayStats(days(i-1))
            val totals = dayKeys.map{k => s"cumulative-$k" -> (dayStat(k) + prevDayStats.getOrElse(s"cumulative-$k", 0L))}.toMap
            cumulDayStats = cumulDayStats + (dt -> (totals ++ dayStat))
          }
        }
      }

      ParsedDailyCountsReport(seenPaths, cumulDayStats)
    }
  }

  private def parseDailyCountsReport(reportResponse: GetReportsResponse): Option[ParsedDailyCountsReport] = {
    for (
      report <- reportResponse.getReports.headOption;
      rows <- Option(report.getData.getRows)
    ) yield {
      val header = report.getColumnHeader
      val dimensions = header.getDimensions

      val dateDimIndex = dimensions.indexOf("ga:date")
      val pathDimIndex = dimensions.indexOf("ga:pagePath")

      val metricHeaders = header.getMetricHeader.getMetricHeaderEntries

      val pageviewsIndex = metricHeaders.indexWhere(_.getName == "pageviews")
      val uniquesIndex = metricHeaders.indexWhere(_.getName == "uniques")

      var seenPaths: Set[String] = Set()
      var dayStats: Map[DateTime, Map[String, Long]] = Map()

      for (
        row <- rows.toList;
        dateRangeValues <- row.getMetrics.headOption
      ) {
        val date = DateTime.parse(row.getDimensions.apply(dateDimIndex), ISODateTimeFormat.basicDate())
        val path = row.getDimensions.apply(pathDimIndex)

        val pageviews = dateRangeValues.getValues.apply(pageviewsIndex).toLong
        val uniques = dateRangeValues.getValues.apply(uniquesIndex).toLong

        seenPaths = seenPaths + path
        val stat: Map[String, Long] = dayStats.get(date).getOrElse(Map())
        val updatedStat: Map[String, Long] = stat ++ Map(s"count$path" -> pageviews, s"unique$path" -> uniques)
        dayStats = dayStats + (date -> updatedStat)
      }

      ParsedDailyCountsReport(seenPaths, dayStats)
    }
  }

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
    def userGlabsAccountWhenAvailable = {
      if (date.exists(_.isAfter(GLABS_VIEW_START_DATE)))
        Config().googleAnalyticsGlabsViewId
      else
        Config().googleAnalyticsViewId
    }

    reportType match {
      case "ctaCtr" => Config().googleAnalyticsViewId
      case "pageViews" => userGlabsAccountWhenAvailable
      case "dailyUniqueUsers" => userGlabsAccountWhenAvailable
      case _ => userGlabsAccountWhenAvailable
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
