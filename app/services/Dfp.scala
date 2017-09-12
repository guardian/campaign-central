package services

import com.google.api.ads.common.lib.auth.OfflineCredentials
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api._
import com.google.api.ads.dfp.axis.factory.DfpServices
import com.google.api.ads.dfp.axis.utils.v201705.StatementBuilder.SUGGESTED_PAGE_LIMIT
import com.google.api.ads.dfp.axis.utils.v201705.{ReportDownloader, StatementBuilder}
import com.google.api.ads.dfp.axis.v201705.Column._
import com.google.api.ads.dfp.axis.v201705.DateRangeType.REACH_LIFETIME
import com.google.api.ads.dfp.axis.v201705.Dimension._
import com.google.api.ads.dfp.axis.v201705.ExportFormat._
import com.google.api.ads.dfp.axis.v201705.{ReportDownloadOptions, ReportJob, ReportQuery, ReportServiceInterface, _}
import com.google.api.ads.dfp.lib.client.DfpSession
import play.api.Logger
import services.Config.conf._

import scala.annotation.tailrec
import scala.io.{BufferedSource, Source}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object Dfp extends DfpService {

  def fetchLineItemById(service: LineItemServiceInterface, id: Long): Option[LineItem] = {
    fetchLineItems(
      service,
      new StatementBuilder().where("id = :id").withBindVariableValue("id", id)
    ).headOption
  }

  def fetchLineItemsByOrder(service: LineItemServiceInterface, orderIds: Seq[Long]): Seq[LineItem] = {
    fetchLineItems(
      service,
      new StatementBuilder().where(s"orderId in (${orderIds.mkString(",")})")
    )
  }

  def fetchSuggestedLineItems(
    campaignName: String,
    clientName: String,
    service: LineItemServiceInterface,
    orderIds: Seq[Long],
    lineItemIdsToIgnore: Seq[Long]
  ): Seq[LineItem] = {

    val fetches: Stream[Seq[LineItem]] = {

      val nameConditions: Seq[String] = {

        def condition(name: String) = s"name like '%${name.toLowerCase}%'"

        def pipedCondition(name: String) = condition(s"| $name |")

        def splitSignificantWords(s: String): Seq[String] = {
          val words = s.split("\\s")
          words
            .map(_.trim.stripSuffix(":"))
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

        Seq(
          Some(pipedCondition(campaignName)),
          Some(pipedCondition(clientName)),
          firstSignificantWord(clientName).map(pipedCondition),
          Some(condition(campaignName)),
          Some(condition(clientName)),
          first2SignificantWordsNameCondition,
          first3SignificantWordsNameCondition,
          firstSignificantWord(clientName).map(condition)
        ).flatten.distinct
      }

      def fetch(nameCondition: String): Stream[Seq[LineItem]] = {

        def fetchByNameAndOrder(nameCondition: String, orderId: Long): Seq[LineItem] = {
          Logger.info(s"Fetching line items to suggest in order $orderId with condition [$nameCondition]")
          val lineItemCondition = {
            if (lineItemIdsToIgnore.isEmpty) ""
            else s"AND NOT id IN (${lineItemIdsToIgnore.mkString(",")})"
          }
          fetchLineItems(
            service,
            new StatementBuilder()
              .where(s"orderId = :orderId $lineItemCondition AND ($nameCondition)")
              .withBindVariableValue("orderId", orderId)
          )
        }

        def isAlreadyLinked(item: LineItem): Boolean = {
          safeSeq(item.getCustomFieldValues) exists (_.getCustomFieldId == dfpCampaignFieldId)
        }

        orderIds.toStream.map { orderId =>
          fetchByNameAndOrder(nameCondition, orderId).filterNot(isAlreadyLinked)
        }
      }

      nameConditions.foldLeft(Stream.empty[Seq[LineItem]])(_ #::: fetch(_))
    }

    fetches.find(_.nonEmpty) getOrElse Nil
  }

  def fetchStatsReport(service: ReportServiceInterface, lineItemIds: Seq[Long]): Option[BufferedSource] = {

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

      val start  = System.currentTimeMillis
      val report = fetchReport(service, qry)
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

  def getCampaignIdCustomField(lineItem: LineItem): Option[BaseCustomFieldValue] =
    safeSeq(lineItem.getCustomFieldValues) find (_.getCustomFieldId == dfpCampaignFieldId)

  def getCampaignIdCustomFieldValue(lineItem: LineItem): Option[String] = {
    getCampaignIdCustomField(lineItem) map {
      _.asInstanceOf[CustomFieldValue].getValue.asInstanceOf[TextValue].getValue
    }
  }

  def hasCampaignIdCustomFieldValue(campaignId: String)(lineItem: LineItem): Boolean =
    getCampaignIdCustomFieldValue(lineItem) contains campaignId

  def linkLineItemToCampaign(service: LineItemServiceInterface, lineItemId: Long, campaignId: String): Unit = {
    fetchLineItemById(service, lineItemId) foreach { item =>
      val currCampaignId = getCampaignIdCustomFieldValue(item)
      if (currCampaignId.isDefined) {
        Logger.error(s"Line item $lineItemId is already linked to campaign ${currCampaignId.get}")
      } else {
        appendCustomFieldToLineItem(service, item, new CustomFieldValue(dfpCampaignFieldId, new TextValue(campaignId)))
      }
    }
  }

  private def appendCustomFieldToLineItem(
    service: LineItemServiceInterface,
    item: LineItem,
    field: CustomFieldValue
  ): Unit = {
    val currFields = safeSeq(item.getCustomFieldValues)
    if (!currFields.contains(field)) {
      item.setCustomFieldValues(currFields.toArray :+ field)
      updateLineItem(service, item)
    }
  }
}

sealed trait DfpService {

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

  def mkLineItemService(session: DfpSession): LineItemServiceInterface =
    new DfpServices().get(session, classOf[LineItemServiceInterface])

  def mkReportService(session: DfpSession): ReportServiceInterface =
    new DfpServices().get(session, classOf[ReportServiceInterface])

  def fetchLineItems(service: LineItemServiceInterface, stmtBuilder: StatementBuilder): Seq[LineItem] = {
    val start = System.currentTimeMillis

    def fetchPage(stmt: Statement): Option[LineItemPage] = {
      val page = Try(service.getLineItemsByStatement(stmt))
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

    val lineItems = fetchResults(stmtBuilder.limit(SUGGESTED_PAGE_LIMIT))
    Logger.info(s"Fetched ${lineItems.size} line items in ${System.currentTimeMillis - start} ms")
    lineItems
  }

  def fetchReport(service: ReportServiceInterface, qry: ReportQuery): Try[BufferedSource] = {

    val reportJob = {
      val job = new ReportJob()
      job.setReportQuery(qry)
      service.runReportJob(job)
    }

    val reportDownloader = new ReportDownloader(service, reportJob.getId)
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

  def updateLineItem(service: LineItemServiceInterface, lineItem: LineItem): Unit = {
    Try(service.updateLineItems(Array(lineItem))) match {
      case Failure(e) =>
        Logger.error(s"Failed to update line item ${lineItem.getId}", e)
      case Success(updated) =>
        if (updated.length != 1) Logger.error(s"Failed to update line item ${lineItem.getId}")
    }
  }

  def safeSeq[T](ts: Array[T]): Seq[T] = Option(ts).map(_.toSeq).getOrElse(Nil)
}

object StopWords {

  val specificExtras = Seq("discover", "new", "test")

  // Taken from http://www.ranks.nl/stopwords
  def apply() =
    Seq(
      "a",
      "about",
      "above",
      "after",
      "again",
      "against",
      "all",
      "am",
      "an",
      "and",
      "any",
      "are",
      "aren't",
      "as",
      "at",
      "be",
      "because",
      "been",
      "before",
      "being",
      "below",
      "between",
      "both",
      "but",
      "by",
      "can't",
      "cannot",
      "could",
      "couldn't",
      "did",
      "didn't",
      "do",
      "does",
      "doesn't",
      "doing",
      "don't",
      "down",
      "during",
      "each",
      "few",
      "for",
      "from",
      "further",
      "had",
      "hadn't",
      "has",
      "hasn't",
      "have",
      "haven't",
      "having",
      "he",
      "he'd",
      "he'll",
      "he's",
      "her",
      "here",
      "here's",
      "hers",
      "herself",
      "him",
      "himself",
      "his",
      "how",
      "how's",
      "i",
      "i'd",
      "i'll",
      "i'm",
      "i've",
      "if",
      "in",
      "into",
      "is",
      "isn't",
      "it",
      "it's",
      "its",
      "itself",
      "let's",
      "me",
      "more",
      "most",
      "mustn't",
      "my",
      "myself",
      "no",
      "nor",
      "not",
      "of",
      "off",
      "on",
      "once",
      "only",
      "or",
      "other",
      "ought",
      "our",
      "ours",
      "ourselves",
      "out",
      "over",
      "own",
      "same",
      "shan't",
      "she",
      "she'd",
      "she'll",
      "she's",
      "should",
      "shouldn't",
      "so",
      "some",
      "such",
      "than",
      "that",
      "that's",
      "the",
      "their",
      "theirs",
      "them",
      "themselves",
      "then",
      "there",
      "there's",
      "these",
      "they",
      "they'd",
      "they'll",
      "they're",
      "they've",
      "this",
      "those",
      "through",
      "to",
      "too",
      "under",
      "until",
      "up",
      "very",
      "was",
      "wasn't",
      "we",
      "we'd",
      "we'll",
      "we're",
      "we've",
      "were",
      "weren't",
      "what",
      "what's",
      "when",
      "when's",
      "where",
      "where's",
      "which",
      "while",
      "who",
      "who's",
      "whom",
      "why",
      "why's",
      "with",
      "won't",
      "would",
      "wouldn't",
      "you",
      "you'd",
      "you'll",
      "you're",
      "you've",
      "your",
      "yours",
      "yourself",
      "yourselves"
    ) ++ specificExtras
}
