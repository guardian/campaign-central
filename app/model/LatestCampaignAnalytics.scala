package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class LatestCampaignAnalytics(
                                    campaignId: String,
                                    uniques: Long,
                                    uniquesFromMobile: Long,
                                    uniquesFromDesktop: Long,
                                    uniquesTarget: Long,
                                    pageviews: Long)

object LatestCampaignAnalytics {
  implicit val latestCampaignAnalyticsFormat: Format[LatestCampaignAnalytics] = Jsonx.formatCaseClass[LatestCampaignAnalytics]
}
