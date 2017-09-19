package model.command

import java.util.UUID

import com.gu.contentapi.client.model.v1.{Tag, TagType, Content => ApiContent}
import com.gu.contentatom.thrift.AtomData
import model._
import model.external.Sponsorship
import org.joda.time.DateTime
import play.api.Logger
import repositories.{CampaignContentRepository, CampaignRepository, PutContentItemResult}

object CommandUtils {

  def deriveHostedTagFromContent(content: List[ApiContent]): Option[Tag] = {
    content.flatMap(_.tags).find { t =>
      t.`type` == TagType.PaidContent
    }
  }

  def deriveContentType(apiContent: ApiContent): Option[String] =
    apiContent.tags
      .find(_.`type` == TagType.Type)
      .map(_.webTitle)

  def deriveSponsorshipLogo(sponsorship: Option[Sponsorship]): Option[String] = {
    sponsorship.flatMap(_.sponsorLogo.assets.headOption.map(_.imageUrl))
  }

  def cleanHeadline(headline: String) = headline match {
    case "" => "untitled"
    case h  => h
  }

  def buildContentItems(apiContent: List[ApiContent], campaignId: String): List[ContentItem] = apiContent.flatMap {
    apic =>
      deriveContentType(apic).map { contentType =>
        ContentItem(
          campaignId = campaignId,
          id = apic.fields.flatMap(_.internalComposerCode).getOrElse(UUID.randomUUID().toString),
          `type` = contentType,
          composerId = apic.fields.flatMap(_.internalComposerCode),
          path = Option(apic.id),
          title = cleanHeadline(apic.webTitle),
          isLive = apic.fields.flatMap(_.isLive).getOrElse(false)
        )
      }
  }

  def updateCampaignAndContent(apiContent: List[ApiContent],
                               campaign: Campaign,
                               sponsorship: Option[Sponsorship],
                               user: User): Either[CampaignCentralApiError, Campaign] = {

    val contentItems: List[ContentItem] = buildContentItems(apiContent, campaign.id)

    val putContentResults: List[Either[CampaignCentralApiError, PutContentItemResult]] = for {
      item <- contentItems
    } yield CampaignContentRepository.putContent(item)

    val results: (List[CampaignCentralApiError], List[PutContentItemResult]) =
      putContentResults.foldRight((List[CampaignCentralApiError](), List[PutContentItemResult]())) {
        case (e, (ls, rs)) => e.fold(l => (l :: ls, rs), r => (ls, r :: rs))
      }

    results match {
      case (firstLeft :: _, _) =>
        Logger.error(s"Failures putting content for campaign ${campaign.id} (${campaign.name}): $firstLeft")
        Left(firstLeft)
      case (Nil, _) =>
        val startDate = apiContent.flatMap(_.fields.flatMap(_.firstPublicationDate)).sortBy(_.dateTime).headOption
        val endDate = sponsorship.flatMap(_.validTo.map(_.withTimeAtStartOfDay().plusDays(1))) orElse Some(
          DateTime.now.plusYears(1)) // give default endDate.

        val status = (startDate, endDate) match {
          case (_, Some(ed)) if ed.isBeforeNow => "dead"
          case (Some(_), _)                    => "live"
          case _                               => "production"
        }

        val updatedCampaign = campaign.copy(
          startDate = startDate.map { cdt =>
            new DateTime(cdt.dateTime).withTimeAtStartOfDay()
          },
          endDate = endDate,
          status = status,
          campaignLogo = deriveSponsorshipLogo(sponsorship) orElse campaign.campaignLogo,
          lastModified = DateTime.now,
          lastModifiedBy = user
        )

        CampaignRepository
          .putCampaign(updatedCampaign)
          .right
          .map(Function.const(updatedCampaign))
    }
  }
}
