package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class CallToAction(
  builderId: Option[String], // the id from the call to action builder
  trackingCode: Option[String] = None
)

object CallToAction {
  implicit val ctaFormat: Format[CallToAction] = Jsonx.formatCaseClass[CallToAction]
}
