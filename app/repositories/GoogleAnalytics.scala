package repositories

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.services.analyticsreporting.v4.model._
import com.google.api.services.analyticsreporting.v4.{AnalyticsReporting, AnalyticsReportingScopes}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import services.{AWS, Config}

import scala.collection.JavaConversions._


object GoogleAnalytics {

  val gaClient = initialiseGaClient

  def getAnalyticsForCampaign(campaignId: String) = {
    for(
      campaign <- CampaignRepository.getCampaign(campaignId);
      startDate <- Some(new DateTime().minusMonths(1) );//campaign.startDate;
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

      for (report <- reportResponse.getReports) yield {
        val header = report.getColumnHeader
        val dimensions = header.getDimensions

        val dateDimIndex = dimensions.indexOf("ga:date")
        val pathDimIndex = dimensions.indexOf("ga:pagePath")

        val metricHeaders = header.getMetricHeader.getMetricHeaderEntries

        val rows = report.getData.getRows

        rows.foreach{ row =>
          val date = DateTime.parse(row.getDimensions.apply(dateDimIndex), ISODateTimeFormat.basicDate())
          val path = row.getDimensions.apply(pathDimIndex)

          val mets = row.getMetrics.map( t => t.getValues.zipWithIndex.map{case (data, idx) => s"${metricHeaders(idx).getName} -> $data"}.mkString(", ")).mkString(":")
          println(s"$path on $date -- $mets")
        }
      }
      "check stdout"
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
