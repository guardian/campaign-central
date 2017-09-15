package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import cats.syntax.either._
import model.command.{CampaignCentralApiError, JsonParsingError}
import play.api.libs.json.{Format, JsValue, Json}

case class ContentItem(campaignId: String,
                       id: String,
                       `type`: String,
                       composerId: Option[String],
                       path: Option[String],
                       title: String,
                       isLive: Boolean) {

  def toItem: Either[CampaignCentralApiError, Item] =
    Option(Item.fromJSON(Json.toJson(this).toString())).map(Right(_)) getOrElse Left(JsonParsingError(""))
}

object ContentItem {
  implicit val contentItemFormat: Format[ContentItem] = Jsonx.formatCaseClass[ContentItem]

  def fromJson(json: JsValue): Either[CampaignCentralApiError, ContentItem] =
    json.asOpt[ContentItem].map(Right(_)) getOrElse Left(JsonParsingError(""))
  def fromItem(item: Item): Either[CampaignCentralApiError, ContentItem] =
    Either.catchNonFatal(Json.parse(item.toJSON).as[ContentItem]).leftMap(e => JsonParsingError(e.getMessage))
}
