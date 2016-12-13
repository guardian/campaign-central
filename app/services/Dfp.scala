package services

import com.google.api.ads.common.lib.auth.OfflineCredentials
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api._
import com.google.api.ads.dfp.axis.factory.DfpServices
import com.google.api.ads.dfp.axis.utils.v201608.StatementBuilder.SUGGESTED_PAGE_LIMIT
import com.google.api.ads.dfp.axis.utils.v201608.{ReportDownloader, StatementBuilder}
import com.google.api.ads.dfp.axis.v201608.Column._
import com.google.api.ads.dfp.axis.v201608.DateRangeType.REACH_LIFETIME
import com.google.api.ads.dfp.axis.v201608.Dimension._
import com.google.api.ads.dfp.axis.v201608.ExportFormat._
import com.google.api.ads.dfp.axis.v201608.{ReportDownloadOptions, ReportJob, ReportQuery, ReportServiceInterface, _}
import com.google.api.ads.dfp.lib.client.DfpSession
import play.api.Logger
import services.Config.conf._

import scala.annotation.tailrec
import scala.io.{BufferedSource, Source}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object Dfp {

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

  def fetchLineItemsByOrder(session: DfpSession, orderIds: Seq[Long]): Seq[LineItem] = {
    fetchLineItems(
      session,
      new StatementBuilder().where(s"orderId in (${orderIds.mkString(",")})")
    )
  }

  def fetchSuggestedLineItems(
    campaignName: String,
    clientName: String,
    session: DfpSession,
    orderIds: Seq[Long]
  ): Seq[LineItem] = {

    def fetchByNameAndOrder(nameCondition: String, orderId: Long): Seq[LineItem] = {
      Logger.info(s"Fetching line items to suggest in order $orderId with condition [$nameCondition]")
      fetchLineItems(
        session,
        new StatementBuilder()
        .where(s"orderId = :orderId AND ($nameCondition)")
        .withBindVariableValue("orderId", orderId)
      )
    }

    val fetches = {

      def nameCondition(name: String) = s"name like '%${name.toLowerCase}%'"

      def pipedNameCondition(name: String) = nameCondition(s"| $name |")

      def splitSignificantWords(s: String): Seq[String] = {
        val words = s.split("\\s")
        words.map(_.trim.stripSuffix(":"))
        .filter(_.nonEmpty)
        .filter(_.head.isUpper)
        .map(_.toLowerCase)
        .filterNot(StopWords().contains)
      }

      def firstSignificantWord(s: String): Option[String] = splitSignificantWords(s).headOption

      lazy val first2SignificantWordsNameCondition: Option[String] = {
        val words = splitSignificantWords(campaignName).take(2)
        if (words.isEmpty) None
        else Some(words.mkString("name like '%", " ", "%'"))
      }

      lazy val first3SignificantWordsNameCondition: Option[String] = {
        val words = splitSignificantWords(campaignName).distinct.take(3)
        if (words.isEmpty) None
        else Some(words.mkString("name like '%", "%' OR name like '%", "%'"))
      }

      def fetch(optName: Option[String]): Stream[Seq[LineItem]] = {

        def isAlreadyLinked(item: LineItem): Boolean = {
          safeSeq(item.getCustomFieldValues) exists (_.getCustomFieldId == dfpCampaignFieldId)
        }

        optName.map(name => orderIds.toStream.map { orderId =>
          fetchByNameAndOrder(name, orderId).filterNot(isAlreadyLinked)
        }).getOrElse(Stream.empty)
      }

      fetch(Some(pipedNameCondition(campaignName))) #:::
      fetch(Some(pipedNameCondition(clientName))) #:::
      fetch(firstSignificantWord(clientName).map(pipedNameCondition)) #:::
      fetch(Some(nameCondition(campaignName))) #:::
      fetch(Some(nameCondition(clientName))) #:::
      fetch(first2SignificantWordsNameCondition) #:::
      fetch(first3SignificantWordsNameCondition) #:::
      fetch(firstSignificantWord(clientName).map(nameCondition))
    }

    fetches.find(_.nonEmpty) getOrElse Nil
  }

  private def fetchLineItems(session: DfpSession, stmtBuilder: StatementBuilder): Seq[LineItem] = {
    val start = System.currentTimeMillis
    val lineItemService = new DfpServices().get(session, classOf[LineItemServiceInterface])

    def fetchPage(stmt: Statement): Option[LineItemPage] = {
      val page = Try(lineItemService.getLineItemsByStatement(stmt))
      page.recoverWith {
        case NonFatal(e) =>
          Logger.error("Fetching line items failed", e)
          page
      }.toOption
    }

    @tailrec
    def fetchResults(stmtBuilder: StatementBuilder, acc: Seq[LineItem] = Nil): Seq[LineItem] = {
      fetchPage(stmtBuilder.toStatement) match {
        case None => acc
        case Some(page) =>
          val pageResults = safeSeq(page.getResults)
          if (pageResults.size < SUGGESTED_PAGE_LIMIT) {
            acc ++ pageResults
          } else {
            fetchResults(stmtBuilder.increaseOffsetBy(SUGGESTED_PAGE_LIMIT), acc ++ pageResults)
          }
      }
    }

    val lineItems = fetchResults(stmtBuilder)
    Logger.info(s"Fetched ${lineItems.size} line items in ${System.currentTimeMillis - start} ms")
    lineItems
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

  def hasCampaignIdCustomFieldValue(campaignId: String)(lineItem: LineItem): Boolean = {
    safeSeq(lineItem.getCustomFieldValues) exists { value =>
      value.getCustomFieldId == dfpCampaignFieldId &&
      value.asInstanceOf[CustomFieldValue].getValue.asInstanceOf[TextValue].getValue.toLowerCase == campaignId
    }
  }

  private def safeSeq[T](ts: Array[T]): Seq[T] = Option(ts).map(_.toSeq).getOrElse(Nil)
}

object StopWords {

  val specificExtras = Seq("discover", "new", "test")

  // Taken from http://www.ranks.nl/stopwords
  def apply() = Seq(
    "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at",
    "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
    "can't", "cannot", "could", "couldn't",
    "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during",
    "each",
    "few", "for", "from", "further",
    "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here",
    "here's", "hers", "herself", "him", "himself", "his", "how", "how's",
    "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself",
    "let's",
    "me", "more", "most", "mustn't", "my", "myself",
    "no", "nor", "not",
    "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own",
    "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such",
    "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these",
    "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too",
    "under", "until", "up",
    "very",
    "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's",
    "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't",
    "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"
  ) ++ specificExtras
}
