package repositories

import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.LatestCampaignAnalyticsItem
<<<<<<< HEAD
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults

object LatestCampaignAnalyticsRepository {

  private implicit val logger: Logger = Logger(getClass)

  private val tableName = Config().latestCampaignAnalyticsTableName

  def getLatestCampaignAnalytics(): Seq[LatestCampaignAnalyticsItem] =
    getResults(Scanamo.scan[LatestCampaignAnalyticsItem](DynamoClient)(tableName))

  def getLatestCampaignAnalytics(campaignId: String): Option[LatestCampaignAnalyticsItem] = {
    val query = 'campaignId -> campaignId
    getResults(Scanamo.query[LatestCampaignAnalyticsItem](DynamoClient)(tableName)(query)).headOption
=======
import model.command.{CampaignCentralApiError, LatestCampaignAnalyticsItemNotFound}
import services.Dynamo
import cats.implicits._

import scala.collection.JavaConverters._

object LatestCampaignAnalyticsRepository {

  def getLatestCampaignAnalytics(): Either[CampaignCentralApiError, List[LatestCampaignAnalyticsItem]] = {
    val results: List[Either[CampaignCentralApiError, LatestCampaignAnalyticsItem]] =
      Dynamo.latestCampaignAnalyticsTable.scan().asScala.toList.map(LatestCampaignAnalyticsItem.fromItem)

    results.sequence
  }

  def getLatestCampaignAnalytics(campaignId: String): Either[CampaignCentralApiError, LatestCampaignAnalyticsItem] = {
    val maybeLatestItemOrError: Either[CampaignCentralApiError, Option[LatestCampaignAnalyticsItem]] = {
      val result: Option[Either[CampaignCentralApiError, LatestCampaignAnalyticsItem]] =
        Dynamo.latestCampaignAnalyticsTable
          .query("campaignId", campaignId)
          .asScala
          .headOption
          .map(LatestCampaignAnalyticsItem.fromItem)

      result.sequence
    }

    maybeLatestItemOrError.flatMap(
      maybeLatestItem =>
        maybeLatestItem.map(Right(_)) getOrElse Left(
          LatestCampaignAnalyticsItemNotFound(s"Could not find latest analytics for campaign with id $campaignId")))
>>>>>>> master
  }
}
