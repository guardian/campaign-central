package repositories

import java.util.concurrent.Executors

import com.google.api.services.analyticsreporting.v4.model._
import model._
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, Duration}
import play.api.Logger
import services.GoogleAnalytics

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}


object Analytics {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

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

      val expiry = campaign.flatMap(calculateValidToDateForDailyStats)

      AnalyticsDataCache.putCampaignDailyCountsReport(campaignId, data, expiry)
      storeSummaryForCampaign(campaignId, data, expiry)
    }

    report
  }

  private def storeSummaryForCampaign(campaignId: String, report: CampaignDailyCountsReport, expiry: Option[Long]) {

    val totalUniques = {
      val uniques = for {
        campaign <- CampaignRepository.getCampaign(campaignId)
        sectionId <- campaign.pathPrefix
      } yield {
        GoogleAnalytics.loadSectionUniquePageViewCount(sectionId, campaign.startDate, campaign.endDate)
      }
      uniques getOrElse 0L
    }

    val targetToDate = {
      val mostRecentStats = report.pageCountStats.last
      mostRecentStats.getOrElse("cumulative-target-uniques", 0L)
    }

    val campaignSummary = CampaignSummary(totalUniques, targetToDate)
    AnalyticsDataCache.putCampaignSummary(campaignId, campaignSummary, expiry)

    updateOverallSummary(campaignId, campaignSummary)
  }

  private def updateOverallSummary(campaignId: String, campaignSummary: CampaignSummary): Unit = {
    val overallSummary = AnalyticsDataCache.getOverallSummary().getOrElse(Map())

    AnalyticsDataCache.putOverallSummary(overallSummary + (campaignId -> campaignSummary))
  }

  private def cleanAndConvertRawDailyCounts(rawDailyCounts: ParsedDailyCountsReport, dailyUniqueTargetValue: Option[Long]): CampaignDailyCountsReport = {
    val cleaned = rawDailyCounts
      .zeroMissingPaths
      .calulateDailyTotals
      .addDailyTargets(dailyUniqueTargetValue)
                  .calculateCumulativeValues

    CampaignDailyCountsReport(cleaned)
  }

  private def loadAnalyticsForHostedCampaign(campaign: Campaign): Option[CampaignDailyCountsReport] = {
    val dailyCounts = for(
      startDate <- campaign.startDate orElse Some(new DateTime().minusMonths(1) );//campaign.startDate;
      gaFilter <- campaign.gaFilterExpression
    ) yield {
      parseDailyCountsReport(GoogleAnalytics.loadDailyCounts(gaFilter, startDate, campaign.endDate))
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
      parseDailyCountsReport(GoogleAnalytics.loadDailyCounts(gaFilter, startDate, campaign.endDate))
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

    def calculateCumulativeValues = {
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


  // CTA clicks functions

  def getCtaClicksForCampaign(campaignId: String): Option[Map[String, Long]] = {

    AnalyticsDataCache.getCampaignCtaClicksReport(campaignId) match {
      case Hit(report) => {
        Logger.debug(s"getting CTA clicks for campaign $campaignId - cache hit")
        Some(report)
      }
      case Stale(report) => {
        Logger.debug(s"getting CTA clicks for campaign $campaignId - cache stale spawning async refresh")

        Future{
          Logger.debug(s"async refresh of CTA clicks for campaign $campaignId")
          fetchAndStoreCtaClicksForCampaign(campaignId)
        } // serve stale but spawn refresh future
        Some(report)
      }
      case Miss => {
        Logger.debug(s"getting CTA clicks for campaign $campaignId - cache miss fetching sync")
        fetchAndStoreCtaClicksForCampaign(campaignId)
      }
    }
  }

  private def fetchAndStoreCtaClicksForCampaign(campaignId: String): Option[Map[String, Long]] = {
    CampaignRepository.getCampaign(campaignId) map { campaign =>
      val reportLines = for (
        cta <- campaign.callToActions;
        startDate <- campaign.startDate;
        builderId <- cta.builderId;
        trackingCode <- cta.trackingCode
      ) yield {
        builderId -> GoogleAnalytics.loadCtaClicks(trackingCode, startDate, campaign.endDate)
      }

      val report = reportLines.toMap

      AnalyticsDataCache.putCampaignCtaClicksReport(campaignId, report, calculateValidToDateForDailyStats(campaign))

      report
    }
  }

  def calculateValidToDateForDailyStats(campaign: Campaign): Option[Long] = {
    val campaignFinished = for (
      d <- campaign.endDate
    ) yield {d.isBeforeNow}

    if (campaignFinished.getOrElse(false)) None
    else {
      Some(DateTime.now().withTimeAtStartOfDay().plusDays(1).getMillis)
    }
  }
}
