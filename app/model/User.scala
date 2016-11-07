package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format
import com.gu.pandomainauth.model.{User => PandaUser}

case class User(
               firstName: String,
               lastName: String,
               email: String
               )

object User {
  implicit val userFormat: Format[User] = Jsonx.formatCaseClass[User]

  def apply(pandaUser: PandaUser): User = User(pandaUser.firstName, pandaUser.lastName, pandaUser.email)
}