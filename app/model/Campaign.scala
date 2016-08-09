package model

import org.joda.time.DateTime
import play.api.libs.json._
import ai.x.play.json.Jsonx
import ai.x.play.json.implicits.optionWithNull

case class Campaign(
                   id: String,
                   name: String,
                   client: Client,
                   content: Option[Content] = None,
                   nominalValue: Option[Long] = None,
                   actualValue: Option[Long] = None,
                   startDate: Option[DateTime] = None,
                   endDate: Option[DateTime] = None,
                   category: Option[String] = None,
                   salesLead: Option[User] = None,
                   targets: List[CampaignTarget] = List(),
                   notes: List[Note] = List()
                   )

object Campaign {
  implicit val campaignFormat: Format[Campaign] = Jsonx.formatCaseClass[Campaign]
}