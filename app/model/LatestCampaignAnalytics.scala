package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class LatestCampaignAnalytics(campaignId: String,
                                   uniques: Long,
                                   uniquesByDevice: Option[Map[String, Long]],
                                   uniquesTarget: Long,
                                   pageviews: Long,
                                   pageviewsByDevice: Option[Map[String, Long]],
                                   medianAttentionTimeSeconds: Option[Long],
                                   medianAttentionTimeByDevice: Option[Map[String, Long]],
                                   weightedAverageDwellTimeForCampaign: Option[Double],
                                   averageDwellTimePerPathSeconds: Option[Map[String, Double]])

object LatestCampaignAnalytics {
  implicit val latestCampaignAnalyticsFormat: Format[LatestCampaignAnalytics] =
    Jsonx.formatCaseClass[LatestCampaignAnalytics]

  def apply(latestCampaignAnalyticsItem: LatestCampaignAnalyticsItem, uniquesTarget: Long): LatestCampaignAnalytics = {

    import util.DoubleUtils._

    LatestCampaignAnalytics(
      latestCampaignAnalyticsItem.campaignId,
      latestCampaignAnalyticsItem.uniques,
      latestCampaignAnalyticsItem.uniquesByDevice.map(normaliseDeviceData),
      uniquesTarget,
      latestCampaignAnalyticsItem.pageviews,
      latestCampaignAnalyticsItem.pageviewsByDevice.map(normaliseDeviceData),
      latestCampaignAnalyticsItem.medianAttentionTimeSeconds,
      latestCampaignAnalyticsItem.medianAttentionTimeByDevice.map(normaliseDeviceData),
      latestCampaignAnalyticsItem.weightedAverageDwellTimeForCampaign.map(_.to2Dp),
      latestCampaignAnalyticsItem.averageDwellTimePerPathSeconds.map(_.mapValues(_.to2Dp))
    )
  }

  private def normaliseDeviceData(data: Map[String, Long]): Map[String, Long] = {
    data.map {
      case (deviceType, medianAttentionTime) =>
        deviceType match {
          case "GUARDIAN_IOS_NATIVE_APP"     => "IOS APP"     -> medianAttentionTime
          case "GUARDIAN_ANDROID_NATIVE_APP" => "ANDROID APP" -> medianAttentionTime
          case "PERSONAL_COMPUTER"           => "DESKTOP"     -> medianAttentionTime
          case "SMARTPHONE"                  => "MOBILE"      -> medianAttentionTime
          case other                         => other         -> medianAttentionTime
        }
    }
  }
}
