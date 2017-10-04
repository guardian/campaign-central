package repositories

import cats.implicits._
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}
import model.{
  CampaignCentralApiError,
  JsonParsingError,
  LatestCampaignAnalyticsItem,
  LatestCampaignAnalyticsItemNotFound
}
import model.{JsonParsingError, LatestCampaignAnalyticsItemNotFound}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object LatestCampaignAnalyticsRepository {

  private val table = Table[LatestCampaignAnalyticsItem](Config().latestCampaignAnalyticsTableName)

  def getLatestCampaignAnalytics(): Either[CampaignCentralApiError, List[LatestCampaignAnalyticsItem]] =
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.scan)).leftMap { e =>
      JsonParsingError(e.show)
    }

  def getLatestCampaignAnalytics(campaignId: String): Either[CampaignCentralApiError, LatestCampaignAnalyticsItem] = {
    Scanamo.exec(DynamoClient)(table.query('campaignId -> campaignId)).headOption map {
      case Left(e)              => Left(JsonParsingError(e.show))
      case Right(analyticsItem) => Right(analyticsItem)
    } getOrElse
      Left(LatestCampaignAnalyticsItemNotFound(s"Could not find latest analytics for campaign with id $campaignId"))
  }
}
