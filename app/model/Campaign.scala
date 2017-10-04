package model

import ai.x.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.json._

case class Campaign(
  id: String,
  name: String,
  `type`: String,
  status: String,
  created: DateTime,
  createdBy: User,
  lastModified: DateTime,
  lastModifiedBy: User,
  campaignLogo: Option[String] = None,
  pathPrefix: String,
  nominalValue: Option[Long] = None,
  actualValue: Option[Long] = None,
  startDate: Option[DateTime] = None,
  endDate: DateTime,
  targets: Map[String, Long] = Map.empty
)

object Campaign {
  implicit val campaignFormat: Format[Campaign]    = Jsonx.formatCaseClass[Campaign]
  implicit val defaultJodaReads: Reads[DateTime]   = JodaReads.DefaultJodaDateTimeReads
  implicit val defaultJodaWrites: Writes[DateTime] = JodaWrites.JodaDateTimeNumberWrites
}
