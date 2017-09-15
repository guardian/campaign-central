package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import org.joda.time.DateTime
import cats.syntax.either._
import model.command.{CampaignCentralApiError, JsonParsingError}
import play.api.libs.json.{Format, JsValue, Json}
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._

case class Campaign(
  id: String,
  name: String,
  `type`: String,
  status: String,
  created: DateTime,
  createdBy: User,
  lastModified: DateTime,
  lastModifiedBy: User,
  tagId: Option[Long] = None,
  campaignLogo: Option[String] = None,
  pathPrefix: Option[String] = None,
  nominalValue: Option[Long] = None,
  actualValue: Option[Long] = None,
  startDate: Option[DateTime] = None,
  endDate: Option[DateTime] = None,
  category: Option[String] = None,
  collaborators: List[User] = Nil,
  targets: Map[String, Long] = Map.empty
) {

  def toItem: Either[CampaignCentralApiError, Item] =
    Option(Item.fromJSON(Json.toJson(this).toString())).map(Right(_)) getOrElse Left(JsonParsingError(""))

  def gaFilterExpression: Option[String] = pathPrefix.map { path =>
    s"ga:pagePath=~/$path"
  }
}

object Campaign {
  implicit val campaignFormat: Format[Campaign] = Jsonx.formatCaseClass[Campaign]

  def fromJson(json: JsValue): Either[CampaignCentralApiError, Campaign] =
    json.asOpt[Campaign].map(Right(_)) getOrElse Left(JsonParsingError(""))

  def fromItem(item: Item): Either[CampaignCentralApiError, Campaign] =
    Either.catchNonFatal(Json.parse(item.toJSON).as[Campaign]).leftMap(e => JsonParsingError(e.getMessage))
}
