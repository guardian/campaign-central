package model.command
import java.util.UUID

import model.command.CommandError._
import ai.x.play.json.Jsonx
import com.gu.contentapi.client.model.v1.{Tag, TagType, Content => ApiContent}
import com.gu.contentatom.thrift.AtomData
import com.gu.contentatom.thrift.atom.media.MediaAtom
import model._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Format
import repositories.{CampaignContentRepository, CampaignRepository, ClientRepository, ContentApi}

case class Section(id: Long, pathPrefix: String)

object Section {
  implicit val sectionFormat: Format[Section] = Jsonx.formatCaseClass[Section]
}

case class ImportCampaignFromCAPICommand(
                                        id: Long,
                                        externalName: String,
                                        section: Section
                                        ) extends Command {
  override type T = Campaign

  def deriveHostedTagFromContent(content: List[ApiContent]) = {
    content.flatMap(_.tags).find{t => t.`type` == TagType.PaidContent && t.paidContentType == Some("HostedContent")}
  }

  def findOrCreateClient(tag: Tag): Client = {
    val sponsorName = tag.activeSponsorships.flatMap(_.headOption.map(_.sponsorName)) getOrElse (SponsorNameNotFound)
    ClientRepository.getClientByName(sponsorName) getOrElse {
      val client = Client(
        id = UUID.randomUUID().toString,
        name = sponsorName,
        country = "UK", // default country and agency, these can be manually
        agency = None   // updated later
      )
      ClientRepository.putClient(client) getOrElse (FailedToSaveClient(client))
    }
  }

  def deriveContentType(apiContent: ApiContent) = {
    apiContent.tags.filter(_.`type` == TagType.Type).headOption.map(_.webTitle).getOrElse(UnableToDetermineContentType)
  }

  def buildAtomList(apiContent: ApiContent): List[Atom] = {
    apiContent.atoms.map{ atoms =>
      val mediaAtoms = atoms.media.map{ mediaAtoms => mediaAtoms.map{ma =>
        Atom(ma.id, "media", Option((ma.data.asInstanceOf[AtomData.Media]).media.title))}
      }.getOrElse(Nil)

      // other content atom types would go here

      mediaAtoms.toList
    }.getOrElse(Nil)
  }

  override def process()(implicit user: Option[User]): Option[Campaign] = {
    Logger.info(s"importing campaign from tag $externalName")

    val userOrDefault = user getOrElse(User("CAPI", "importer", "labs.beta@guardian.co.uk"))
    val now = DateTime.now

    val apiContent = ContentApi.loadAllContentInSection(section.pathPrefix)
    val hostedTag = deriveHostedTagFromContent(apiContent) getOrElse (CampaignTagNotFound)

    val campaign = CampaignRepository.getCampaignByTag(id) getOrElse {
      Campaign(
        id = UUID.randomUUID().toString,
        name = externalName,
        status = "pending",
        tagId = Some(id),
        clientId = findOrCreateClient(hostedTag).id,
        created = now,
        createdBy = userOrDefault,
        lastModified = now,
        lastModifiedBy = userOrDefault,
        nominalValue = Some(10000),         // default targets and values
        actualValue = Some(10000),          // these will be set in the UI manually
        targets = Map("uniques" -> 10000L)
      )
    }

    val contentItems = apiContent.map{ apic =>
      ContentItem(
        campaignId = campaign.id,
        id = apic.fields.flatMap(_.internalComposerCode).getOrElse(UUID.randomUUID().toString),
        `type` = deriveContentType(apic),
        composerId = apic.fields.flatMap(_.internalComposerCode),
        path = Option(apic.id),
        title = apic.webTitle,
        isLive = apic.fields.flatMap(_.isLive).getOrElse(false),
        atoms = buildAtomList(apic)
      )
    }

    contentItems.foreach( CampaignContentRepository.putContent )

    val startDate = apiContent.flatMap(_.fields.flatMap(_.firstPublicationDate)).sortBy(_.dateTime).headOption

    val updatedCampaign = campaign.copy(
      startDate = startDate.map{cdt => new DateTime(cdt.dateTime).withTimeAtStartOfDay()},
      status = if(startDate.isDefined) "live" else campaign.status
    )

    CampaignRepository.putCampaign(updatedCampaign)
  }
}


object ImportCampaignFromCAPICommand {
  implicit val importCampaignFromCAPICommandFormat: Format[ImportCampaignFromCAPICommand] = Jsonx.formatCaseClass[ImportCampaignFromCAPICommand]
}