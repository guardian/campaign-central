package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format
import services.CampaignService.DeviceBreakdown

case class LatestCampaignAnalytics(campaignId: String,
                                   uniques: Long,
                                   uniquesFromMobile: Long,
                                   uniquesFromDesktop: Long,
                                   uniquesTarget: Long,
                                   pageviews: Long,
                                   medianAttentionTimeSeconds: Option[Long],
                                   medianAttentionTimeByDevice: Option[Map[String, Long]],
                                   weightedAverageDwellTimeForCampaign: Option[Double],
                                   averageDwellTimePerPathSeconds: Option[Map[String, Double]])

object LatestCampaignAnalytics {
  implicit val latestCampaignAnalyticsFormat: Format[LatestCampaignAnalytics] =
    Jsonx.formatCaseClass[LatestCampaignAnalytics]

  def apply(latestCampaignAnalyticsItem: LatestCampaignAnalyticsItem,
            deviceBreakdown: DeviceBreakdown,
            uniquesTarget: Long): LatestCampaignAnalytics = {

    import util.DoubleUtils._

    LatestCampaignAnalytics(
      latestCampaignAnalyticsItem.campaignId,
      latestCampaignAnalyticsItem.uniques,
      deviceBreakdown.mobile,
      deviceBreakdown.desktop,
      uniquesTarget,
      latestCampaignAnalyticsItem.pageviews,
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
