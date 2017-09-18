package repositories

import cats.implicits._
import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.LatestCampaignAnalyticsItem
import model.command.{CampaignCentralApiError, JsonParsingError, LatestCampaignAnalyticsItemNotFound}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object LatestCampaignAnalyticsRepository {

  private val tableName = Config().latestCampaignAnalyticsTableName

  def getLatestCampaignAnalytics(): Either[CampaignCentralApiError, List[LatestCampaignAnalyticsItem]] =
    getResultsOrFirstFailure(Scanamo.scan[LatestCampaignAnalyticsItem](DynamoClient)(tableName)).left map { e =>
      JsonParsingError(e.show)
    }

  def getLatestCampaignAnalytics(campaignId: String): Either[CampaignCentralApiError, LatestCampaignAnalyticsItem] = {
    Scanamo.query[LatestCampaignAnalyticsItem](DynamoClient)(tableName)('campaignId -> campaignId).headOption map {
      case Left(e)              => Left(JsonParsingError(e.show))
      case Right(analyticsItem) => Right(analyticsItem)
    } getOrElse
      Left(LatestCampaignAnalyticsItemNotFound(s"Could not find latest analytics for campaign with id $campaignId"))
  }
}
