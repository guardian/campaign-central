package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format



case class CampaignTarget(
                          targetType: String,
                          value: Long
                         )

object CampaignTarget {
  implicit val campaignTargetFormat: Format[CampaignTarget] = Jsonx.formatCaseClass[CampaignTarget]
}