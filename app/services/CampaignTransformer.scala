package services

import java.util.UUID

import com.gu.contentapi.client.model.v1.{Section => CapiSection}
import model.{Campaign, User}
import org.joda.time.DateTime
import repositories.contentapi.CapiSectionTransformer

object CampaignTransformer {

  def createDefaultCampaign(section: CapiSection): Option[Campaign] = {

    val now: DateTime = DateTime.now
    val user          = User("Campaign Central", "Admin", "commercial.dev@theguardian.com")

    val startDate = CapiSectionTransformer.deriveStartDate(section)
    val endDate   = CapiSectionTransformer.deriveEndDateOrDefaultToNow(section)
    val campaignName = CapiSectionTransformer.deriveCampaignName(section)

    for {
      sponsorshipType <- CapiSectionTransformer.deriveSponsorshipType(section)
      logo = CapiSectionTransformer.deriveSponsorshipLogo(section)
    } yield {
      Campaign(
        id = UUID.randomUUID().toString,
        name = campaignName,
        `type` = sponsorshipType,
        status = CapiSectionTransformer.deriveStatus(startDate, endDate),
        pathPrefix = section.id,
        created = now,
        createdBy = user,
        lastModified = now,
        lastModifiedBy = user,
        nominalValue = None, // these will be set in the UI manually
        actualValue = None, // these will be set in the UI manually
        campaignLogo = logo,
        startDate = startDate,
        endDate = endDate,
        targets = Map.empty
      )
    }
  }

  def updateExistingCampaign(section: CapiSection, campaign: Campaign, user: User): Campaign = {

    val startDate = CapiSectionTransformer.deriveStartDate(section)
    val endDate   = CapiSectionTransformer.deriveEndDateOrDefaultToNow(section)

    campaign.copy(
      name = CapiSectionTransformer.deriveCampaignName(section),
      `type` = CapiSectionTransformer.deriveSponsorshipType(section) getOrElse campaign.`type`,
      status = CapiSectionTransformer.deriveStatus(startDate, endDate),
      pathPrefix = section.id,
      campaignLogo = CapiSectionTransformer.deriveSponsorshipLogo(section) orElse campaign.campaignLogo,
      startDate = startDate,
      endDate = endDate,
      lastModified = DateTime.now,
      lastModifiedBy = user
    )

  }

}
