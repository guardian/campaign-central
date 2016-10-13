package repositories

import ai.x.play.json.Jsonx
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.services.analyticsreporting.v4.model._
import com.google.api.services.analyticsreporting.v4.{AnalyticsReporting, AnalyticsReportingScopes}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.libs.json.Format
import repositories.GoogleAnalytics.ParsedDailyCountsReport
import services.{AWS, Config}

import scala.collection.JavaConversions._


case class CampaignDailyCountsReport(seenPaths: Set[String], pageCountStats: List[Map[String, Long]])

object CampaignDailyCountsReport{
  implicit val agencyFormat: Format[CampaignDailyCountsReport] = Jsonx.formatCaseClass[CampaignDailyCountsReport]

  def apply(parsedDailyCountsReport: ParsedDailyCountsReport): CampaignDailyCountsReport = {
    CampaignDailyCountsReport(
      parsedDailyCountsReport.seenPaths,
      parsedDailyCountsReport.dayStats.keySet.toList.sortBy(_.getMillis).map{ dt =>
        val stats = parsedDailyCountsReport.dayStats(dt)
        stats + ("date" -> dt.getMillis)
      }
    )
  }
}

object GoogleAnalytics {

  val gaClient = initialiseGaClient

  def getAnalyticsForCampaign(campaignId: String): Option[CampaignDailyCountsReport] = {
    Logger.info(s"fetch ga analytics for campaign $campaignId")
    val report = for(
      campaign <- CampaignRepository.getCampaign(campaignId);
      startDate <- campaign.startDate orElse Some(new DateTime().minusMonths(1) );//campaign.startDate;
      gaFilter <- campaign.gaFilterExpression
    ) yield {

      val endOfRange = campaign.endDate.flatMap{ed => if(ed.isBeforeNow) Some(ed.toString("yyyy-MM-dd")) else None}.getOrElse("today")
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

      val stats = parseDailyCountsReport(reportResponse)
        .map(_.zeroMissingPaths)
        .map(_.calulateDailyTotals)
        .map(_.calulateCumalativeVales)

      stats.map(CampaignDailyCountsReport(_))
    }

    report.flatten
  }

  case class ParsedDailyCountsReport(seenPaths: Set[String], dayStats: Map[DateTime, Map[String, Long]]) {

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
    for (report <- reportResponse.getReports.headOption) yield {
      val header = report.getColumnHeader
      val dimensions = header.getDimensions

      val dateDimIndex = dimensions.indexOf("ga:date")
      val pathDimIndex = dimensions.indexOf("ga:pagePath")

      val metricHeaders = header.getMetricHeader.getMetricHeaderEntries

      val pageviewsIndex = metricHeaders.indexWhere(_.getName == "pageviews")
      val uniquesIndex = metricHeaders.indexWhere(_.getName == "uniques")

      val rows = report.getData.getRows

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

  private def initialiseGaClient = {

    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val credential = GoogleCredential.fromStream(Config().googleServiceAccountJsonInputStream)
    val scoped = credential.createScoped(List(AnalyticsReportingScopes.ANALYTICS_READONLY))

    new AnalyticsReporting.Builder(
        httpTransport,
        com.google.api.client.googleapis.util.Utils.getDefaultJsonFactory,
        scoped)
      .setApplicationName("campaign central")
      .build()
  }


}
