package model

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.libs.json.{Json, Reads}
import cats.syntax.either._

case class CampaignReferralRow(
  campaignId: String,
  hash: String,
  referralDate: String,
  platform: String,
  edition: Option[String],
  path: Option[String],
  containerIndex: Option[Int],
  containerName: Option[String],
  cardIndex: Option[Int],
  cardName: Option[String],
  clicks: Long,
  unparsedComponent: Option[String]
)

object CampaignReferralRow {

  implicit val itemReads: Reads[CampaignReferralRow] = Json.reads[CampaignReferralRow]

  def fromItem(item: Item): Either[CampaignCentralApiError, CampaignReferralRow] =
    Either.catchNonFatal(Json.parse(item.toJSON).as[CampaignReferralRow]).leftMap(e => JsonParsingError(e.getMessage))
}
