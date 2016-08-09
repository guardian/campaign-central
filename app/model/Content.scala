package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class Content (
                      path: String
                   )

object Content {
  implicit val contentFormat: Format[Content] = Jsonx.formatCaseClass[Content]
}