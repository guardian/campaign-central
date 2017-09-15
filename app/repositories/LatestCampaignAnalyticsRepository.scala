package repositories

import model.LatestCampaignAnalyticsItem
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
  }

}
