package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format
import util.DoubleUtils._

case class Totals(uniques: Long, pageviews: Long, timeSpentOnPage: Double)
object Totals {
  implicit val totalsFormat: Format[Totals] = Jsonx.formatCaseClass[Totals]

  def apply(latestAnalytics: Seq[LatestCampaignAnalytics]): Totals = {
    Totals(
      uniques = latestAnalytics.map(_.uniques).sum,
      pageviews = latestAnalytics.map(_.pageviews).sum,
      timeSpentOnPage = latestAnalytics.flatMap(_.weightedAverageDwellTimeForCampaign).sum.to2Dp
    )
  }
}

case class Averages(uniques: Long, pageviews: Long, timeSpentOnPage: Double)
object Averages {
  implicit val averagesFormat: Format[Averages] = Jsonx.formatCaseClass[Averages]

  def apply(latestAnalytics: Seq[LatestCampaignAnalytics]): Averages = {
    val noOfAnalytics = latestAnalytics.size.toLong
    Averages(
      uniques = latestAnalytics.map(_.uniques).sum / noOfAnalytics,
      pageviews = latestAnalytics.map(_.pageviews).sum / noOfAnalytics,
      timeSpentOnPage = (latestAnalytics.flatMap(_.weightedAverageDwellTimeForCampaign).sum / noOfAnalytics).to2Dp
    )
  }
}

case class PaidFor(totals: Totals, averages: Averages)
object PaidFor {
  implicit val paidForFormat: Format[PaidFor] = Jsonx.formatCaseClass[PaidFor]
}
case class Hosted(totals: Totals, averages: Averages)
object Hosted {
  implicit val hostedFormat: Format[Hosted] = Jsonx.formatCaseClass[Hosted]
}

case class Benchmarks(totals: Totals, averages: Averages, paidFor: PaidFor, hosted: Hosted)

object Benchmarks {
  implicit val benchmarksFormat: Format[Benchmarks] = Jsonx.formatCaseClass[Benchmarks]
}
