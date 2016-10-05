package model

import org.joda.time.DateTime
import play.api.libs.json._
import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger

import scala.util.control.NonFatal

case class Campaign(
                   id: String,
                   name: String,
                   status: String,
                   clientId: String,
                   created: DateTime,
                   createdBy: User,
                   lastModified: DateTime,
                   lastModifiedBy: User,
                   tagId: Option[Long] = None,
                   callToActions: List[CallToAction] = Nil,
                   nominalValue: Option[Long] = None,
                   actualValue: Option[Long] = None,
                   startDate: Option[DateTime] = None,
                   endDate: Option[DateTime] = None,
                   category: Option[String] = None,
                   collaborators: List[User] = Nil,
                   targets: Map[String, Long] = Map.empty
                   ) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())

  def gaFilterExpression: Option[String] = Some("ga:pagePath=~/advertiser-content/visit-britain")
}

object Campaign {
  implicit val campaignFormat: Format[Campaign] = Jsonx.formatCaseClass[Campaign]

  def fromJson(json: JsValue) = json.as[Campaign]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[Campaign]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load campaign ${item.toJSON}", e)
      throw e
    }
  }
}