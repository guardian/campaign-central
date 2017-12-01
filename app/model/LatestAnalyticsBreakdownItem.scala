package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class LatestAnalyticsBreakdownItem(uniques: Long, pageviews: Long, timeSpentOnPage: Option[Double] = None)

object LatestAnalyticsBreakdownItem {

  import cats.Semigroup
  import cats._
  import cats.implicits._

  implicit val latestCampaignAnalyticsBreakdownItemFormat: Format[LatestAnalyticsBreakdownItem] =
    Jsonx.formatCaseClass[LatestAnalyticsBreakdownItem]

  /*
   * It's important to consider the context when you use this method. This method combines uniques but its not always
   * accurate to do so.
   */
  implicit val latestAnalyticsMonoid: Monoid[LatestAnalyticsBreakdownItem] =
    new Monoid[LatestAnalyticsBreakdownItem] {
      override def combine(itemOne: LatestAnalyticsBreakdownItem,
                           itemTwo: LatestAnalyticsBreakdownItem): LatestAnalyticsBreakdownItem = {
        LatestAnalyticsBreakdownItem(
          uniques = itemOne.uniques + itemTwo.uniques,
          pageviews = itemOne.pageviews + itemTwo.pageviews,
          timeSpentOnPage = Semigroup[Option[Double]].combine(itemOne.timeSpentOnPage, itemTwo.timeSpentOnPage)
        )
      }

      override def empty = LatestAnalyticsBreakdownItem(0, 0, None)
    }

  def sortByUniques: ((String, LatestAnalyticsBreakdownItem), (String, LatestAnalyticsBreakdownItem)) => Boolean = {
    case ((_, lb1), (_, lb2)) => lb1.uniques > lb2.uniques
  }

}
