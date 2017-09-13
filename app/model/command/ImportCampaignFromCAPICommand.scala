package model.command
import java.util.UUID

import ai.x.play.json.Jsonx
import com.gu.contentapi.client.model.v1.{Tag, TagType, Content => ApiContent}
import com.gu.contentatom.thrift.AtomData
import model._
import model.command.CommandError._
import model.external.Sponsorship
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Format
import repositories._

case class Section(id: Long, pathPrefix: String)

object Section {
  implicit val sectionFormat: Format[Section] = Jsonx.formatCaseClass[Section]
}

trait CAPIImportCommand {

  def deriveHostedTagFromContent(content: List[ApiContent]): Option[Tag] = {
    content.flatMap(_.tags).find { t =>
      t.`type` == TagType.PaidContent
    }
  }

  def deriveContentType(apiContent: ApiContent) = {
    apiContent.tags
      .find(_.`type` == TagType.Type)
      .map(_.webTitle)
      .getOrElse(throw new RuntimeException(s"Unable to derive content type from ${apiContent.id}"))
  }

  def deriveSponsorshipLogo(sponsorship: Option[Sponsorship]): Option[String] = {
    sponsorship.flatMap(_.sponsorLogo.assets.headOption.map(_.imageUrl))
  }

  def buildAtomList(apiContent: ApiContent): List[Atom] = {
    apiContent.atoms
      .map { atoms =>
        val mediaAtoms = atoms.media
          .map { mediaAtoms =>
            mediaAtoms.map { ma =>
              Atom(ma.id, "media", Option(ma.data.asInstanceOf[AtomData.Media].media.title))
            }
          }
          .getOrElse(Nil)

        // other content atom types would go here

        mediaAtoms.toList
      }
      .getOrElse(Nil)
  }

  def cleanHeadline(headline: String) = headline match {
    case "" => "untitled"
    case h  => h
  }

  def buildContentItems(apiContent: List[ApiContent], campaignId: String): List[ContentItem] = apiContent.map { apic =>
    ContentItem(
      campaignId = campaignId,
      id = apic.fields.flatMap(_.internalComposerCode).getOrElse(UUID.randomUUID().toString),
      `type` = deriveContentType(apic),
      composerId = apic.fields.flatMap(_.internalComposerCode),
      path = Option(apic.id),
      title = cleanHeadline(apic.webTitle),
      isLive = apic.fields.flatMap(_.isLive).getOrElse(false),
      atoms = buildAtomList(apic)
    )
  }

  case class UpdateCampaignSuccess(updatedCampaign: Campaign)

  def updateCampaignAndContent(
    apiContent: List[ApiContent],
    campaign: Campaign,
    sponsorship: Option[Sponsorship],
    user: User): Either[CampaignCentralApiError, UpdateCampaignSuccess] = {
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
        val endDate   = sponsorship.flatMap(_.validTo.map(_.withTimeAtStartOfDay().plusDays(1)))

        val status = (startDate, endDate) match {
          case (_, Some(ed)) if ed.isBeforeNow => "dead"
          case (Some(_), _)                    => "live"
          case _                               => "production"
        }

        val ctaAtoms = apiContent.flatMap(_.atoms.flatMap(_.cta)).flatten

        val updatedCampaign = campaign.copy(
          startDate = startDate.map { cdt =>
            new DateTime(cdt.dateTime).withTimeAtStartOfDay()
          },
          endDate = endDate,
          status = status,
          callToActions = ctaAtoms.map { atomData =>
            val ctaAtom = atomData.data.asInstanceOf[AtomData.Cta]
            ctaAtom.cta.trackingCode
            CallToAction(Some(atomData.id), ctaAtom.cta.trackingCode)
          }.distinct,
          campaignLogo = deriveSponsorshipLogo(sponsorship) orElse campaign.campaignLogo,
          lastModified = DateTime.now,
          lastModifiedBy = user
        )

        CampaignRepository
          .putCampaign(updatedCampaign)
          .right
          .map(Function.const(UpdateCampaignSuccess(updatedCampaign)))
    }
  }
}

case class ImportTag(id: Long, externalName: String, section: Section)

object ImportTag {
  implicit val importTagFormat: Format[ImportTag] = Jsonx.formatCaseClass[ImportTag]
}

case class ImportCampaignFromCAPICommand(
  tag: ImportTag,
  campaignValue: Long,
  uniquesTarget: Long,
  pageviewTarget: Option[Long]
) extends CAPIImportCommand {

  sealed trait ImportCampaignResult

  def process()(implicit user: Option[User]): Either[CampaignCentralApiError, Campaign] = {
    Logger.info(s"importing campaign from tag ${tag.externalName}")

    val userOrDefault = user getOrElse User("CAPI", "importer", "labs.beta@guardian.co.uk")
    val now           = DateTime.now

    val apiContent: List[ApiContent] = ContentApi.loadAllContentInSection(tag.section.pathPrefix)
    deriveHostedTagFromContent(apiContent).toRight(CampaignTagNotFound(tag.id, tag.externalName)).right.flatMap {
      hostedTag =>
        val sponsorship = TagManagerApi.getSponsorshipForTag(tag.id)

        val campaignType = hostedTag.paidContentType match {
          case Some("HostedContent") => "hosted"
          case Some(_)               => "paidContent"
          case None                  => InvalidCampaignTagType
        }

        val campaign = CampaignRepository.getCampaignByTag(tag.id) getOrElse {
          Campaign(
            id = UUID.randomUUID().toString,
            name = tag.externalName,
            `type` = campaignType,
            status = "pending",
            tagId = Some(tag.id),
            pathPrefix = Some(tag.section.pathPrefix),
            created = now,
            createdBy = userOrDefault,
            lastModified = now,
            lastModifiedBy = userOrDefault,
            nominalValue = None, // default targets and values
            actualValue = Some(campaignValue), // these will be set in the UI manually
            targets = (Some("uniques" -> uniquesTarget) ++ pageviewTarget.map("pageviews" -> _)).toMap
          )
        }

        updateCampaignAndContent(apiContent, campaign, sponsorship, userOrDefault).right.map(_.updatedCampaign)
    }
  }
}

object ImportCampaignFromCAPICommand {
  implicit val importCampaignFromCAPICommandFormat: Format[ImportCampaignFromCAPICommand] =
    Jsonx.formatCaseClass[ImportCampaignFromCAPICommand]
}

case class RefreshCampaignSuccess(campaign: Campaign)

case class RefreshCampaignFromCAPICommand(id: String) extends CAPIImportCommand {

  def process(): Either[CampaignCentralApiError, RefreshCampaignSuccess] = {
    CampaignRepository.getCampaign(id) match {
      case Some(campaign) =>
        Logger.info(s"refreshing campaign ${campaign.name} (${campaign.id})")

        val apiContent: List[ApiContent] =
          ContentApi.loadAllContentInSection(campaign.pathPrefix getOrElse CampaignMissingData("pathPrefix"))
        val sponsorship = campaign.tagId.flatMap(TagManagerApi.getSponsorshipForTag)

        // TODO: Pass along user for lastModified
        updateCampaignAndContent(apiContent, campaign, sponsorship).right.map(updateCampaignSuccess =>
          RefreshCampaignSuccess(updateCampaignSuccess.updatedCampaign))

      case None => Left(CampaignNotFound(s"Campaign ID $id not found"))
    }
  }
}

object RefreshCampaignFromCAPICommand {
  implicit val refreshCampaignFromCAPICommandFormat: Format[RefreshCampaignFromCAPICommand] =
    Jsonx.formatCaseClass[RefreshCampaignFromCAPICommand]
}
