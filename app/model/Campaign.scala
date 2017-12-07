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
  startDate: Option[DateTime] = None,
  endDate: DateTime,
  targets: Map[String, Long] = Map.empty,
  campaignTargets: Option[Map[String, Map[String, Long]]] = None,
  productionOffice: Option[String]
)

object Campaign {
  implicit val campaignFormat: Format[Campaign]    = Jsonx.formatCaseClass[Campaign]
  implicit val defaultJodaReads: Reads[DateTime]   = JodaReads.DefaultJodaDateTimeReads
  implicit val defaultJodaWrites: Writes[DateTime] = JodaWrites.JodaDateTimeNumberWrites
}
