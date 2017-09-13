package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class Agency(
  id: String,
  name: String
)

object Agency {
  implicit val agencyFormat: Format[Agency] = Jsonx.formatCaseClass[Agency]
}
