package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class LastExecuted(lastExecuted: String)

object LastExecuted {
  implicit val LastExecutedItemFormat: Format[LastExecuted] = Jsonx.formatCaseClass[LastExecuted]
}
