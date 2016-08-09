package model

import org.joda.time.DateTime
import play.api.libs.json._
import ai.x.play.json.Jsonx

case class Campaign(
                   id: String,
                   name: String,
                   client: Client,
                   created: DateTime,
                   createdBy: User,
                   lastModified: DateTime,
                   lastModifiedBy: User,
                   tagId: Option[Long] = None,
                   content: List[ContentItem] = Nil,
                   callToActions: List[CallToAction] = Nil,
                   nominalValue: Option[Long] = None,
                   actualValue: Option[Long] = None,
                   startDate: Option[DateTime] = None,
                   endDate: Option[DateTime] = None,
                   category: Option[String] = None,
                   collaborators: List[User] = Nil,
                   targets: List[CampaignTarget] = List(),
                   notes: List[Note] = List()
                   )

object Campaign {
  implicit val campaignFormat: Format[Campaign] = Jsonx.formatCaseClass[Campaign]
}