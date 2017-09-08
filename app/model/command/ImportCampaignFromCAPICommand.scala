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

  def deriveContentType(apiContent: ApiContent): String = {
    apiContent.tags.find(_.`type` == TagType.Type).map(_.webTitle).getOrElse(UnableToDetermineContentType)
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

  def cleanHeadline(headline: String): String = headline match {
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

  def updateCampaignAndContent(campaignRepository: CampaignRepository,
                               campaignContentRepository: CampaignContentRepository,
                               apiContent: List[ApiContent],
                               campaign: Campaign,
                               sponsorship: Option[Sponsorship]): Option[Campaign] = {
    val contentItems = buildContentItems(apiContent, campaign.id)

    contentItems.foreach(campaignContentRepository.putContent)

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
      campaignLogo = deriveSponsorshipLogo(sponsorship) orElse campaign.campaignLogo
    )

    campaignRepository.putCampaign(updatedCampaign)
  }

}

case class ImportCampaignFromCAPICommand(id: Long, externalName: String, section: Section) extends CAPIImportCommand {

  def findOrCreateClient(clientRepository: ClientRepository, sponsorship: Option[Sponsorship]): Client = {
    val sponsorName = sponsorship.map(_.sponsorName) getOrElse SponsorNameNotFound
    clientRepository.getClientByName(sponsorName) getOrElse {
      val client = Client(
        id = UUID.randomUUID().toString,
        name = sponsorName,
        country = "UK", // default country and agency, these can be manually
        agency = None // updated later
      )
      clientRepository.putClient(client) getOrElse FailedToSaveClient(client)
    }
  }

  def process(
    campaignRepository: CampaignRepository,
    campaignContentRepository: CampaignContentRepository,
    clientRepository: ClientRepository,
    contentApi: ContentApi,
    tagManagerApi: TagManagerApi
  )(implicit user: Option[User]): Either[CommandError, Option[Campaign]] = {
    Logger.info(s"importing campaign from tag $externalName")

    val userOrDefault = user getOrElse User("CAPI", "importer", "labs.beta@guardian.co.uk")
    val now           = DateTime.now

    val apiContent = contentApi.loadAllContentInSection(section.pathPrefix)
    deriveHostedTagFromContent(apiContent).toRight(CampaignTagNotFound).right map { hostedTag =>
      val sponsorship = tagManagerApi.getSponsorshipForTag(id)

      val campaignType = hostedTag.paidContentType match {
        case Some("HostedContent") => "hosted"
        case Some(_)               => "paidContent"
        case None                  => InvalidCampaignTagType
      }

      val campaign = campaignRepository.getCampaignByTag(id) getOrElse {
        Campaign(
          id = UUID.randomUUID().toString,
          name = externalName,
          `type` = campaignType,
          status = "pending",
          tagId = Some(id),
          pathPrefix = Some(section.pathPrefix),
          clientId = findOrCreateClient(clientRepository, sponsorship).id,
          created = now,
          createdBy = userOrDefault,
          lastModified = now,
          lastModifiedBy = userOrDefault,
          nominalValue = Some(10000), // default targets and values
          actualValue = Some(10000), // these will be set in the UI manually
          targets = Map("uniques" -> 10000L)
        )
      }

      updateCampaignAndContent(campaignRepository, campaignContentRepository, apiContent, campaign, sponsorship)
    }
  }
}

object ImportCampaignFromCAPICommand {
  implicit val importCampaignFromCAPICommandFormat: Format[ImportCampaignFromCAPICommand] =
    Jsonx.formatCaseClass[ImportCampaignFromCAPICommand]
}

case class RefreshCampaignFromCAPICommand(id: String) extends CAPIImportCommand {

  def process(
    campaignRepository: CampaignRepository,
    campaignContentRepository: CampaignContentRepository,
    contentApi: ContentApi,
    tagManagerApi: TagManagerApi
  )(implicit user: Option[User]): Either[CommandError, Option[Campaign]] = {
    val campaign = campaignRepository.getCampaign(id) getOrElse { CampaignNotFound }

    Logger.info(s"refreshing campaign ${campaign.name} (${campaign.id})")

    val apiContent =
      contentApi.loadAllContentInSection(campaign.pathPrefix getOrElse CampaignMissingData("pathPrefix"))
    val sponsorship = campaign.tagId.flatMap(tagManagerApi.getSponsorshipForTag)

    Right(updateCampaignAndContent(campaignRepository, campaignContentRepository, apiContent, campaign, sponsorship))
  }
}

object RefreshCampaignFromCAPICommand {
  implicit val refreshCampaignFromCAPICommandFormat: Format[RefreshCampaignFromCAPICommand] =
    Jsonx.formatCaseClass[RefreshCampaignFromCAPICommand]
}
