package repositories

import cats.implicits._
import com.gu.scanamo._
import com.gu.scanamo.syntax._
import model.{CampaignCentralApiError, CampaignNotFound, JsonParsingError}
import play.api.libs.json.Json
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignMediaEventsRepository {

  case class PageMedia(
    mediaId: String,
    path: String,
    playEvent: Long,
    percent25: Long,
    percent50: Long,
    percent75: Long,
    endEvent: Long
  )
  case class CampaignMediaEvents(
    campaignId: String,
    campaignName: String,
    pages: Seq[PageMedia]
  )

  object PageMedia {
    implicit val PageMediaFormat = Json.format[PageMedia]
  }
  object CampaignMediaEvents {
    implicit val CampaignMediaEventsFormat = Json.format[CampaignMediaEvents]
  }

  private val table = Table[CampaignMediaEvents](Config().campaignMediaEventsTableName)

  def getCampaignMediaEvents(campaignId: String): Either[CampaignCentralApiError, CampaignMediaEvents] = {
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.query('campaignId -> campaignId))) match {
      case Left(e)          => Left(JsonParsingError(e.show))
      case Right(Nil)       => Left(CampaignNotFound(s"campaign $campaignId has no media events"))
      case Right(head :: _) => Right(head)
    }
  }
}
