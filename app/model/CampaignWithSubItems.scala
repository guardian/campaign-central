package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format


case class CampaignWithSubItems(
                                 campaign: Campaign,
                                 content: List[ContentItem] = Nil,
                                 notes: List[Note] = Nil
                               )

object CampaignWithSubItems {
  implicit val campaignWithSubItemsFormat: Format[CampaignWithSubItems] = Jsonx.formatCaseClass[CampaignWithSubItems]
}
