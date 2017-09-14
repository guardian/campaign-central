package repositories

import java.util.concurrent.Executors

import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.Campaign
import model.reports._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.{Json, Reads}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.{getResult, getResults}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

sealed trait CacheResult[+A] extends Product {

  def isEmpty: Boolean

  def get: A

  def getOrElse[B >: A](default: => B): B =
    if (isEmpty) default else this.get
}

case class Hit[+A](x: A) extends CacheResult[A] {
  def isEmpty = false
  def get: A  = x
}

case class Stale[+A](x: A) extends CacheResult[A] {
  def isEmpty = false
  def get: A  = x
}

case object Miss extends CacheResult[Nothing] {
  def isEmpty = true
  def get     = throw new NoSuchElementException("Miss.get")
}

object AnalyticsDataCache {

  implicit val analyticsExecutionContext: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(50))

  private implicit val logger: Logger = Logger(getClass)

  private val tableName = Config().analyticsDataCacheTableName

  def deleteCacheEntry(key: String, dataType: String): Unit =
    Scanamo.delete(DynamoClient)(tableName)('key -> key and 'dataType -> dataType)

  private def putEntry(entry: AnalyticsDataCacheEntry): Unit = Scanamo.put(DynamoClient)(tableName)(entry)

  def putCampaignPageViewsReport(
    campaignId: String,
    data: CampaignPageViewsReport,
    validToTimestamp: Option[Long]
  ): Unit =
    putEntry(
      AnalyticsDataCacheEntry(
        campaignId,
        "CampaignPageViewsReport",
        Json.toJson(data).toString(),
        validToTimestamp,
        System.currentTimeMillis()
      )
    )

  def putDailyUniqueUsersReport(
    campaignId: String,
    data: DailyUniqueUsersReport,
    validToTimestamp: Option[Long]
  ): Unit =
    putEntry(
      AnalyticsDataCacheEntry(
        campaignId,
        "DailyUniqueUsersReport",
        Json.toJson(data).toString(),
        validToTimestamp,
        System.currentTimeMillis()
      )
    )

  def putCampaignSummary(campaignId: String, data: CampaignSummary, validToTimestamp: Option[Long]): Unit =
    putEntry(
      AnalyticsDataCacheEntry(
        campaignId,
        "CampaignSummary",
        Json.toJson(data).toString(),
        validToTimestamp,
        System.currentTimeMillis()
      )
    )

  def putOverallSummary(data: OverallSummaryReport): Unit =
    putEntry(
      AnalyticsDataCacheEntry(
        "overall",
        "CampaignSummary",
        Json.toJson(data).toString(),
        None,
        System.currentTimeMillis()
      )
    )

  def putCampaignCtaClicksReport(campaignId: String, data: CtaClicksReport, validToTimestamp: Option[Long]): Unit =
    putEntry(
      AnalyticsDataCacheEntry(
        campaignId,
        "CtaClicksReport",
        Json.toJson(data).toString(),
        validToTimestamp,
        System.currentTimeMillis()
      )
    )

  def putQualifiedPercentagesReport(
    campaignId: String,
    data: QualifiedPercentagesReport,
    validToTimestamp: Option[Long]
  ): Unit =
    putEntry(
      AnalyticsDataCacheEntry(
        campaignId,
        "QualifiedPercentagesReport",
        Json.toJson(data).toString(),
        validToTimestamp,
        System.currentTimeMillis()
      )
    )

  private def getEntry[T](key: String, dataType: String)(implicit fjs: Reads[T]): CacheResult[T] = {
    val item = Scanamo
      .get[AnalyticsDataCacheEntry](DynamoClient)(tableName)('key -> key and 'dataType -> dataType)
      .flatMap(getResult(_))
    item
      .map { entry =>
        val report = Json.parse(entry.data).as[T]
        entry.expires match {
          case Some(ts) if ts < System.currentTimeMillis() => Stale(report)
          case _                                           => Hit(report)
        }
      }
      .getOrElse(Miss)
  }

  def getCampaignPageViewsReport(campaignId: String): CacheResult[CampaignPageViewsReport] = {
    getEntry[CampaignPageViewsReport](campaignId, "CampaignPageViewsReport")
  }

  def getDailyUniqueUsersReport(campaignId: String): CacheResult[DailyUniqueUsersReport] = {
    getEntry[DailyUniqueUsersReport](campaignId, "DailyUniqueUsersReport")
  }

  def getCampaignSummary(campaignId: String): CacheResult[CampaignSummary] = {
    getEntry[CampaignSummary](campaignId, "CampaignSummary")
  }

  def getOverallSummary(): CacheResult[OverallSummaryReport] = {
    getEntry[OverallSummaryReport]("overall", "CampaignSummary")
  }

  def getCampaignCtaClicksReport(campaignId: String): CacheResult[CtaClicksReport] = {
    getEntry[CtaClicksReport](campaignId, "CtaClicksReport")
  }

  def getCampaignQualifiedPercentagesReport(campaignId: String): CacheResult[QualifiedPercentagesReport] = {
    getEntry[QualifiedPercentagesReport](campaignId, "QualifiedPercentagesReport")
  }

  def summariseContents: List[AnalyticsDataCacheEntrySummary] =
    getResults(Scanamo.scan[AnalyticsDataCacheEntry](DynamoClient)(tableName))
      .map { entry =>
        AnalyticsDataCacheEntrySummary(
          key = entry.key,
          dataType = entry.dataType,
          expires = entry.expires,
          written = entry.written
        )
      }
      .sortBy(_.key)

  def calculateValidToDateForDailyStats(campaign: Campaign): Option[Long] = {
    val campaignFinished = for (d <- campaign.endDate) yield { d.isBeforeNow }

    if (campaignFinished.getOrElse(false)) None
    else { Some(DateTime.now().withTimeAtStartOfDay().plusDays(1).getMillis) }
  }
}
