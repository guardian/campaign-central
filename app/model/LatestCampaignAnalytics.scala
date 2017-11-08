package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

import scala.collection.immutable.ListMap

case class LatestAnalyticsBreakdownItem(uniques: Long,
                                        pageviews: Long,
                                        timeSpentOnPage: Option[Double] = None,
                                        facebookShares: Option[Long] = None,
                                        linkedInShares: Option[Long] = None)

object LatestAnalyticsBreakdownItem {
  implicit val latestCampaignAnalyticsBreakdownItemFormat: Format[LatestAnalyticsBreakdownItem] =
    Jsonx.formatCaseClass[LatestAnalyticsBreakdownItem]
}

case class LatestCampaignAnalytics(campaignId: String,
                                   uniques: Long,
                                   uniquesByDevice: Option[Map[String, Long]],
                                   uniquesTarget: Long,
                                   pageviews: Long,
                                   pageviewsByDevice: Option[Map[String, Long]],
                                   medianAttentionTimeSeconds: Option[Long],
                                   medianAttentionTimeByDevice: Option[Map[String, Long]],
                                   weightedAverageDwellTimeForCampaign: Option[Double],
                                   averageDwellTimePerPathSeconds: Option[Map[String, Double]],
                                   analyticsByCountryCode: Map[String, LatestAnalyticsBreakdownItem],
                                   analyticsByDevice: Map[String, LatestAnalyticsBreakdownItem],
                                   analyticsByPath: Map[String, LatestAnalyticsBreakdownItem])

object LatestCampaignAnalytics {
  implicit val latestCampaignAnalyticsFormat: Format[LatestCampaignAnalytics] =
    Jsonx.formatCaseClass[LatestCampaignAnalytics]

  def apply(latestCampaignAnalyticsItem: LatestCampaignAnalyticsItem, uniquesTarget: Long): LatestCampaignAnalytics = {

    import util.DoubleUtils._

    def sortByUniques: ((String, LatestAnalyticsBreakdownItem), (String, LatestAnalyticsBreakdownItem)) => Boolean = {
      case ((_, lb1), (_, lb2)) => lb1.uniques > lb2.uniques
    }

    val metricsByCountryCode: Map[String, LatestAnalyticsBreakdownItem] =
      latestCampaignAnalyticsItem.uniquesByCountryCode.flatMap {
        case (key, uniques) =>
          val maybePageviews = latestCampaignAnalyticsItem.pageviewsByCountryCode.get(key)
          maybePageviews.map { pageviews =>
            key -> LatestAnalyticsBreakdownItem(uniques, pageviews)
          }
      }

    val top20PlusSumOfOtherMetricsByCountryCode: Map[String, LatestAnalyticsBreakdownItem] = {
      val sortedMetricsByCountryCode = ListMap(metricsByCountryCode.toSeq.sortWith(sortByUniques): _*)
      val (top20, other)             = sortedMetricsByCountryCode.splitAt(20)

      top20 + ("other" ->
        LatestAnalyticsBreakdownItem(
          uniques = other.values.map(_.uniques).sum,
          pageviews = other.values.map(_.pageviews).sum,
          timeSpentOnPage = Some(other.values.flatMap(_.timeSpentOnPage).sum)
        ))
    }

    val metricsByDevice: Map[String, LatestAnalyticsBreakdownItem] =
      latestCampaignAnalyticsItem.uniquesByDevice.getOrElse(Map.empty).flatMap {
        case (key, uniques) =>
          val maybePageviews = latestCampaignAnalyticsItem.pageviewsByDevice.flatMap(_.get(key))
          maybePageviews.map { pageviews =>
            key -> LatestAnalyticsBreakdownItem(uniques, pageviews)
          }
      }

    val metricsByPath: Map[String, LatestAnalyticsBreakdownItem] =
      latestCampaignAnalyticsItem.averageDwellTimePerPathSeconds.getOrElse(Map.empty).map {
        case (path, averageTimeSpentOnPage) =>
          val pageviews = latestCampaignAnalyticsItem.pageviewsByPath.flatMap(_.get(path)).getOrElse(0L)
          val uniques   = latestCampaignAnalyticsItem.uniquesByPath.flatMap(_.get(path)).getOrElse(0L)

          path -> LatestAnalyticsBreakdownItem(uniques, pageviews, Some(averageTimeSpentOnPage.to2Dp))
      }

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
      latestCampaignAnalyticsItem.averageDwellTimePerPathSeconds.map(_.mapValues(_.to2Dp)),
      top20PlusSumOfOtherMetricsByCountryCode,
      ListMap(metricsByDevice.toSeq.sortWith(sortByUniques): _*),
      ListMap(metricsByPath.toSeq.sortWith(sortByUniques): _*)
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
