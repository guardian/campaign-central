package repositories.contentapi

import com.gu.contentapi.client.model.v1.{Section => CapiSection}
import org.joda.time.DateTime

object CapiSectionTransformer {

  def deriveStartDate(section: CapiSection): Option[DateTime] =
    section.activeSponsorships.getOrElse(Nil).flatMap(_.validFrom).headOption.map(dt => new DateTime(dt.dateTime))

  def deriveEndDateOrDefaultToNow(section: CapiSection): DateTime = {
    section.activeSponsorships
      .getOrElse(Nil)
      .flatMap(_.validTo)
      .headOption
      .map(dt => new DateTime(dt.dateTime)) getOrElse DateTime.now.plusYears(1)
  }

  def deriveCampaignName(section: CapiSection): String = section.webTitle

  def deriveSponsorshipType(section: CapiSection): Option[String] = {
    def isHosted(section: CapiSection): Boolean = section.id.startsWith("advertiser-content")
    if (isHosted(section)) Some("hosted")
    else section.activeSponsorships.flatMap(_.headOption.map(_.sponsorshipType.name.toLowerCase))
  }

  def deriveSponsorshipLogo(section: CapiSection): Option[String] =
    section.activeSponsorships.getOrElse(Nil).headOption.map(sponsorship => sponsorship.sponsorLogo)

  def deriveStatus(startDate: Option[DateTime], endDate: DateTime): String = {
    (startDate, endDate) match {
      case (_, ed) if ed.isBeforeNow => "dead"
      case (Some(_), _) | (None, _)  => "live"
      case _                         => "unknown"
    }
  }

}
