package model.external

import ai.x.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.json.Format

case class SponsorshipTargeting(publishedSince: Option[DateTime], validEditions: Option[List[String]])

object SponsorshipTargeting {
  implicit val sponsorshipTargetingFormat: Format[SponsorshipTargeting]  = Jsonx.formatCaseClass[SponsorshipTargeting]
}


case class Sponsorship (
                         id: Long,
                         validFrom: Option[DateTime],
                         validTo: Option[DateTime],
                         status: String,
                         sponsorshipType: String,
                         sponsorName: String,
                         sponsorLogo: Image,
                         highContrastSponsorLogo: Option[Image],
                         sponsorLink: String,
                         aboutLink: Option[String],
                         tags: Option[List[Long]],
                         sections: Option[List[Long]],
                         targeting: Option[SponsorshipTargeting])

object Sponsorship {
  implicit val sponsorshipFormat: Format[Sponsorship] = Jsonx.formatCaseClass[Sponsorship]
}