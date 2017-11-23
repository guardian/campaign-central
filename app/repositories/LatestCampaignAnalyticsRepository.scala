package repositories

import cats.implicits._
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}
import model._
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object LatestCampaignAnalyticsRepository {

  private val table = Table[LatestCampaignAnalyticsItem](Config().latestCampaignAnalyticsTableName)

  def getLatestCampaignAnalytics(
    territory: Territory): Either[CampaignCentralApiError, List[LatestCampaignAnalyticsItem]] = {
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.query('territory -> territory.databaseKeyValue)))
      .leftMap { e =>
        JsonParsingError(e.show)
      }
  }

  def getLatestCampaignAnalytics(campaignId: String,
                                 territory: Territory): Either[CampaignCentralApiError, LatestCampaignAnalyticsItem] = {
    Scanamo
      .exec(DynamoClient)(table.query('territory -> territory.databaseKeyValue and 'campaignId -> campaignId))
      .headOption map {
      case Left(e)              => Left(JsonParsingError(e.show))
      case Right(analyticsItem) => Right(analyticsItem)
    } getOrElse
      Left(LatestCampaignAnalyticsItemNotFound(s"Could not find latest analytics for campaign with id $campaignId"))
  }
}
