package model.command
import java.util.UUID

import ai.x.play.json.Jsonx
import com.gu.contentapi.client.model.v1.TagType
import model.{Agency, Campaign, Client, User}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Format
import repositories.{CampaignRepository, ContentApi}

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

  override def process()(implicit user: Option[User]): Option[Campaign] = {
    Logger.info(s"importing campaign from tag $externalName")

    val userOrDefault = user getOrElse(User("CAPI", "importer", "labs.beta@guardian.co.uk"))
    val now = DateTime.now

    val content = ContentApi.loadAllContentInSection(section.pathPrefix)
    val hostedTag = content.flatMap(_.tags).find{t => t.`type` == TagType.PaidContent && t.paidContentType == Some("HostedContent")}


    val campaign = CampaignRepository.getCampaignByTag(id) getOrElse {
      Campaign(
        id = UUID.randomUUID().toString,
        name = externalName,
        status = "pending",
        client = Client(UUID.randomUUID().toString, "Carmaker", "UK", Some(Agency(UUID.randomUUID().toString, "OMG"))),
        created = now,
        createdBy = userOrDefault,
        lastModified = now,
        lastModifiedBy = userOrDefault,
        nominalValue = Some(10000),
        actualValue = Some(10000),
        targets = Map("uniques" -> 10000L)
      )
    }

    println(hostedTag)
    None
  }
}


object ImportCampaignFromCAPICommand {
  implicit val importCampaignFromCAPICommandFormat: Format[ImportCampaignFromCAPICommand] = Jsonx.formatCaseClass[ImportCampaignFromCAPICommand]
}