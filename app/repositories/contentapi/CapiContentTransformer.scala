package repositories.contentapi

import java.util.UUID
import com.gu.contentapi.client.model.v1.{Tag => CapiTag, Content => CapiContent, TagType => CapiTagType}
import model.ContentItem

object CapiContentTransformer {

  def deriveHostedTagFromContent(content: List[CapiContent]): Option[CapiTag] = {
    content.flatMap(_.tags).find { t =>
      t.`type` == CapiTagType.PaidContent
    }
  }

  def deriveContentType(apiContent: CapiContent): Option[String] =
    apiContent.tags
      .find(_.`type` == CapiTagType.Type)
      .map(_.webTitle)

  def cleanHeadline(headline: String) = headline match {
    case "" => "untitled"
    case h  => h
  }

  def buildContentItems(apiContent: List[CapiContent], campaignId: String): List[ContentItem] = apiContent.flatMap {
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

}
