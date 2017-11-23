package model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format
import util.DoubleUtils._

import scala.util.Try

object Ops {
  def sumOrZero[T](op: => T)(default: T): T = Try(op).toOption.getOrElse(default)
}

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
      uniques = Ops.sumOrZero[Long](latestAnalytics.map(_.uniques).sum / noOfAnalytics)(0),
      pageviews = Ops.sumOrZero[Long](latestAnalytics.map(_.pageviews).sum / noOfAnalytics)(0),
      timeSpentOnPage =
        Ops.sumOrZero[Double](latestAnalytics.flatMap(_.weightedAverageDwellTimeForCampaign).sum / noOfAnalytics)(0.0).to2Dp
    )
  }
}

case class Medians(attentionTime: Double)
object Medians {
  implicit val mediansFormat: Format[Medians] = Jsonx.formatCaseClass[Medians]

  def apply(latestAnalytics: Seq[LatestCampaignAnalytics]): Medians = {
    Medians(
      attentionTime = {
        val attnTimes      = latestAnalytics.flatMap(_.medianAttentionTimeSeconds)
        val (lower, upper) = attnTimes.sorted.splitAt(attnTimes.size / 2)

        if (attnTimes.size % 2 == 0) {
          (lower.lastOption.map(_.toDouble).getOrElse(0.0) + upper.headOption.map(_.toDouble).getOrElse(0.0)) / 2.0
        } else upper.headOption.map(_.toDouble).getOrElse(0.0)
      }
    )
  }
}

case class PaidFor(totals: Totals, averages: Averages, medians: Medians)
object PaidFor {
  implicit val paidForFormat: Format[PaidFor] = Jsonx.formatCaseClass[PaidFor]
}
case class Hosted(totals: Totals, averages: Averages, medians: Medians)
object Hosted {
  implicit val hostedFormat: Format[Hosted] = Jsonx.formatCaseClass[Hosted]
}

case class Benchmarks(totals: Totals, averages: Averages, medians: Medians, paidFor: PaidFor, hosted: Hosted)

object Benchmarks {
  implicit val benchmarksFormat: Format[Benchmarks] = Jsonx.formatCaseClass[Benchmarks]
}
