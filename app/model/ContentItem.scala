package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class ContentItem(
                        composerId: String
                      )

object ContentItem {
  implicit val contentItemFormat: Format[ContentItem] = Jsonx.formatCaseClass[ContentItem]
}