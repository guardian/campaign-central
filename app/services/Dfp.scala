package services

import com.google.api.ads.common.lib.auth.OfflineCredentials
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api._
import com.google.api.ads.dfp.axis.factory.DfpServices
import com.google.api.ads.dfp.axis.utils.v201608.{ReportDownloader, StatementBuilder}
import com.google.api.ads.dfp.axis.v201608.Column._
import com.google.api.ads.dfp.axis.v201608.DateRangeType.REACH_LIFETIME
import com.google.api.ads.dfp.axis.v201608.Dimension._
import com.google.api.ads.dfp.axis.v201608.ExportFormat._
import com.google.api.ads.dfp.axis.v201608.{ReportDownloadOptions, ReportJob, ReportQuery, ReportServiceInterface, _}
import com.google.api.ads.dfp.lib.client.DfpSession
import play.api.Logger
import services.Config.conf._

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

object DfpFetcher {

  System.setProperty("api.dfp.soapRequestTimeout", "300000")

  def mkSession(): DfpSession = {
    new DfpSession.Builder()
    .withOAuth2Credential(
      new OfflineCredentials.Builder()
      .forApi(DFP)
      .withClientSecrets(dfpClientId, dfpClientSecret)
      .withRefreshToken(dfpRefreshToken)
      .build()
      .generateCredential()
    )
    .withApplicationName(dfpAppName)
    .withNetworkCode(dfpNetworkCode)
    .build()
  }

  def fetchLineItemsByOrder(session: DfpSession, orderIds: Set[Long]): Seq[LineItem] = {

    val start = System.currentTimeMillis
    val lineItems = fetchLineItems(
      session,
      new StatementBuilder()
      .where(s"orderId in (${orderIds.mkString(",")})")
      .toStatement
    )

    lineItems match {
      case Failure(e) =>
        Logger.error(s"Fetching line items for orders $orderIds failed: ${e.getMessage}")
      case Success(items) =>
        Logger.info(s"Fetched ${items.size} line items for orders $orderIds in ${System.currentTimeMillis - start} ms")
    }

    lineItems getOrElse Nil
  }

  private def fetchLineItems(session: DfpSession, statement: Statement): Try[Seq[LineItem]] = {
    val lineItemService = new DfpServices().get(session, classOf[LineItemServiceInterface])
    Try(lineItemService.getLineItemsByStatement(statement)) map { page =>

      // assuming only one page of results
      page.getResults.toSeq
    }
  }

  def fetchStatsReport(session: DfpSession, lineItemIds: Seq[Long]): Option[BufferedSource] = {

    if (lineItemIds.isEmpty) None

    else {

      val qry = new ReportQuery()
      qry.setDateRangeType(REACH_LIFETIME)
      qry.setStatement(
        new StatementBuilder()
        .where(s"LINE_ITEM_ID IN (${lineItemIds.mkString(",")})")
        .toStatement
      )
      qry.setDimensions(Array(DATE))
      qry.setColumns(
        Array(
          TOTAL_INVENTORY_LEVEL_IMPRESSIONS,
          TOTAL_INVENTORY_LEVEL_CLICKS
        )
      )

      val start = System.currentTimeMillis
      val report = fetchReport(session, qry)
      report match {
        case Failure(e) =>
          Logger.error(s"Stats report on line items ${lineItemIds.mkString(", ")} failed: ${e.getMessage}")
        case Success(_) =>
          Logger.info(
            s"Stats report on line items ${lineItemIds.mkString(", ")} took ${System.currentTimeMillis - start} ms"
          )
      }
      report.toOption
    }
  }

  private def fetchReport(session: DfpSession, qry: ReportQuery): Try[BufferedSource] = {

    val reportService = new DfpServices().get(session, classOf[ReportServiceInterface])

    val reportJob = {
      val job = new ReportJob()
      job.setReportQuery(qry)
      reportService.runReportJob(job)
    }

    val reportDownloader = new ReportDownloader(reportService, reportJob.getId)
    Try(reportDownloader.waitForReportReady()) map { completed =>

      val source = {
        val options = new ReportDownloadOptions()
        options.setExportFormat(CSV_DUMP)
        options.setUseGzipCompression(false)
        reportDownloader.getDownloadUrl(options)
      }

      Source.fromURL(source)
    }
  }
}

object DfpFilter {

  def hasCampaignIdCustomFieldValue(campaignId: String)(lineItem: LineItem): Boolean = {
    safeSeq(lineItem.getCustomFieldValues) exists { value =>
      value.getCustomFieldId == dfpCampaignFieldId &&
      value.asInstanceOf[CustomFieldValue].getValue.asInstanceOf[TextValue].getValue.toLowerCase == campaignId
    }
  }

  private def safeSeq[T](ts: Array[T]): Seq[T] = Option(ts).map(_.toSeq).getOrElse(Nil)
}
