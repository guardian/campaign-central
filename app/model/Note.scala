package model

import ai.x.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.json.Format


case class Note(
               created: DateTime,
               createdBy: User,
               lastModified: DateTime,
               lastModifiedBy: User,
               content: String
               )

object Note {
  implicit val noteFormat: Format[Note] = Jsonx.formatCaseClass[Note]
}