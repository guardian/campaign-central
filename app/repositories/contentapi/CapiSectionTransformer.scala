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

  def deriveSponsorName(section: CapiSection): Option[String] =
    section.activeSponsorships.flatMap(_.headOption.map(_.sponsorName))

  def deriveSponsorshipType(section: CapiSection): Option[String] =
    section.activeSponsorships.flatMap(_.headOption.map(_.sponsorshipType.name))

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
