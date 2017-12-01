package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

import scala.collection.immutable.ListMap

case class LatestCampaignAnalytics(campaignId: String,
                                   uniques: Long,
                                   uniquesTarget: Long,
                                   pageviews: Long,
                                   medianAttentionTimeSeconds: Option[Long],
                                   weightedAverageDwellTimeForCampaign: Option[Double],
                                   analyticsByCountryCode: Map[String, LatestAnalyticsBreakdownItem],
                                   analyticsByDevice: Map[String, LatestAnalyticsBreakdownItem],
                                   analyticsByPath: Map[String, LatestAnalyticsBreakdownItem])

object LatestCampaignAnalytics {
  implicit val latestCampaignAnalyticsFormat: Format[LatestCampaignAnalytics] =
    Jsonx.formatCaseClass[LatestCampaignAnalytics]

  private val LogicalDeviceGroupings: Map[String, List[String]] = Map(
    "Desktop" -> List("PERSONAL_COMPUTER"),
    "Tablet"  -> List("TABLET"),
    "Mobile"  -> List("SMARTPHONE"),
    "App"     -> List("GUARDIAN_IOS_NATIVE_APP", "GUARDIAN_ANDROID_NATIVE_APP"),
    "Other"   -> List("GAME_CONSOLE", "OTHER", "UNKNOWN")
  )

  def apply(latestCampaignAnalyticsItem: LatestCampaignAnalyticsItem, uniquesTarget: Long): LatestCampaignAnalytics = {

    import util.DoubleUtils._
    import cats.implicits._
    import model.LatestAnalyticsBreakdownItem._

    val top20PlusSumOfOtherMetricsByCountryCode: Map[String, LatestAnalyticsBreakdownItem] = {

      val metricsByCountryCode: Map[String, LatestAnalyticsBreakdownItem] =
        latestCampaignAnalyticsItem.uniquesByCountryCode
          .map { uniquesByCountryCode =>
            uniquesByCountryCode.flatMap {
              case (key, uniques) =>
                val maybePageviews = latestCampaignAnalyticsItem.pageviewsByCountryCode.flatMap(_.get(key))
                val maybeTimeSpentOnPage =
                  latestCampaignAnalyticsItem.averageDwellTimePerCountryCodeSeconds.flatMap(_.get(key))
                maybePageviews.map { pageviews =>
                  key -> LatestAnalyticsBreakdownItem(uniques, pageviews, maybeTimeSpentOnPage)
                }
            }
          }
          .getOrElse(Map.empty)

      val sortedMetricsByCountryCode = ListMap(metricsByCountryCode.toSeq.sortWith(sortByUniques): _*)
      val (top20, other)             = sortedMetricsByCountryCode.splitAt(20)

      top20 + ("Other" ->
        LatestAnalyticsBreakdownItem(
          uniques = other.values.map(_.uniques).sum,
          pageviews = other.values.map(_.pageviews).sum,
          timeSpentOnPage = Some(other.values.flatMap(_.timeSpentOnPage).sum)
        ))
    }

    val metricsByDevice: Map[String, LatestAnalyticsBreakdownItem] = {
      LogicalDeviceGroupings.map {
        case (label, grouping) =>
          val breakdownSummary: LatestAnalyticsBreakdownItem = grouping.flatMap { key =>
            for {
              uniques   <- latestCampaignAnalyticsItem.uniquesByDevice.flatMap(_.get(key))
              pageviews <- latestCampaignAnalyticsItem.pageviewsByDevice.flatMap(_.get(key))
              maybeTimeSpentOnPage = latestCampaignAnalyticsItem.averageDwellTimePerDeviceSeconds.flatMap(_.get(key))
            } yield {
              LatestAnalyticsBreakdownItem(uniques, pageviews, maybeTimeSpentOnPage)
            }
          }.combineAll

          label -> breakdownSummary
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
      uniquesTarget,
      latestCampaignAnalyticsItem.pageviews,
      latestCampaignAnalyticsItem.medianAttentionTimeSeconds,
      latestCampaignAnalyticsItem.weightedAverageDwellTimeForCampaign.map(_.to2Dp),
      top20PlusSumOfOtherMetricsByCountryCode,
      ListMap(metricsByDevice.toSeq.sortWith(sortByUniques): _*),
      ListMap(metricsByPath.toSeq.sortWith(sortByUniques): _*)
    )
  }

}
