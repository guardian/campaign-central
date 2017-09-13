package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class Atom(id: String, `type`: String, title: Option[String])

object Atom {
  implicit val atomFormat: Format[Atom] = Jsonx.formatCaseClass[Atom]
}