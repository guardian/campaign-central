package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class Client(
                 id: String,
                 name: String,
                 country: String,
                 agency: Option[Agency] = None
                 )


object Client {
  implicit val clientFormat: Format[Client] = Jsonx.formatCaseClass[Client]
}