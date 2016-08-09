package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class User(
               firstName: String,
               lastName: String,
               email: String
               )

object User {
  implicit val userFormat: Format[User] = Jsonx.formatCaseClass[User]
}