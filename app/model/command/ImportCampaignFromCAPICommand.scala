package model.command
import java.util.UUID

import ai.x.play.json.Jsonx
import com.gu.contentapi.client.model.v1.{Content => ApiContent}
import model._
import model.external.Sponsorship
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Format
import repositories._

case class Section(id: Long, pathPrefix: String)

object Section {
  implicit val sectionFormat: Format[Section] = Jsonx.formatCaseClass[Section]
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
) {
  
  def process()(implicit user: User): Either[CampaignCentralApiError, Campaign] = {
    Logger.info(s"importing campaign from tag ${tag.externalName}")

    val now: DateTime = DateTime.now

    val apiContent: List[ApiContent] = ContentApi.loadAllContentInSection(tag.section.pathPrefix)
    CommandUtils
      .deriveHostedTagFromContent(apiContent)
      .toRight(CampaignTagNotFound(tag.id, tag.externalName))
      .right
      .flatMap { hostedTag =>
        val sponsorship: Option[Sponsorship] = TagManagerApi.getSponsorshipForTag(tag.id)

        val campaignType: Either[CampaignCentralApiError, String] = hostedTag.paidContentType match {
          case Some("HostedContent") => Right("hosted")
          case Some(_)               => Right("paidContent")
          case None                  => Left(InvalidCampaignTagType)
        }

        campaignType.right.flatMap { campaignType =>
          val campaign = CampaignRepository.getCampaignByTag(tag.id) getOrElse {
            Campaign(
              id = UUID.randomUUID().toString,
              name = tag.externalName,
              `type` = campaignType,
              status = "pending",
              tagId = Some(tag.id),
              pathPrefix = Some(tag.section.pathPrefix),
              created = now,
              createdBy = user,
              lastModified = now,
              lastModifiedBy = user,
              nominalValue = None, // default targets and values
              actualValue = Some(campaignValue), // these will be set in the UI manually
              targets = (Some("uniques" -> uniquesTarget) ++ pageviewTarget.map("pageviews" -> _)).toMap
            )
          }

          CommandUtils.updateCampaignAndContent(apiContent, campaign, sponsorship, user)
        }
      }
  }
}

object ImportCampaignFromCAPICommand {
  implicit val importCampaignFromCAPICommandFormat: Format[ImportCampaignFromCAPICommand] =
    Jsonx.formatCaseClass[ImportCampaignFromCAPICommand]
}

case class RefreshCampaignFromCAPICommand(id: String) {

  def process()(implicit user: User): Either[CampaignCentralApiError, Campaign] = {
    CampaignRepository.getCampaign(id) match {
      case Some(campaign) =>
        campaign.pathPrefix.map(ContentApi.loadAllContentInSection(_)) match {
          case Some(content) =>
            val sponsorship = campaign.tagId.flatMap(TagManagerApi.getSponsorshipForTag)

            Logger.info(s"refreshing campaign ${campaign.name} (${campaign.id})")
            CommandUtils.updateCampaignAndContent(content, campaign, sponsorship, user)
          case None =>
            Logger.error(s"Campaign ${campaign.id} (${campaign.name}) is missing pathPrefix")
            Left(CampaignMissingPathPrefix(campaign))
        }

      case None => Left(CampaignNotFound(s"Campaign ID $id not found"))
    }
  }
}

object RefreshCampaignFromCAPICommand {
  implicit val refreshCampaignFromCAPICommandFormat: Format[RefreshCampaignFromCAPICommand] =
    Jsonx.formatCaseClass[RefreshCampaignFromCAPICommand]
}
