package model

import java.time.LocalDate

import com.google.api.ads.dfp.axis.v201608.{DateTime, LineItem}
import play.api.libs.json.{Json, Writes}
import services.Config.conf._

case class PerformanceStats(impressions: Int, clicks: Int) {
  val ctr: Double = if (impressions == 0) 0 else clicks.toDouble / impressions * 100
}

object PerformanceStats {

  implicit val writes = new Writes[PerformanceStats] {
    def writes(stats: PerformanceStats) = Json.obj(
      "impressions" -> stats.impressions,
      "clicks" -> stats.clicks,
      "ctr" -> stats.ctr
    )
  }

  def sum(stats: Seq[PerformanceStats]) = PerformanceStats(
    impressions = stats.map(_.impressions).sum,
    clicks = stats.map(_.clicks).sum
  )
}

case class TrafficDriver(
  id: Long,
  name: String,
  url: String,
  status: String,
  startDate: LocalDate,
  endDate: LocalDate,
  summaryStats: PerformanceStats
)

object TrafficDriver {

  def fromDfpLineItem(lineItem: LineItem): TrafficDriver = {

    def mkLocalDate(dfpDateTime: DateTime): LocalDate = {
      val date = dfpDateTime.getDate
      LocalDate.of(date.getYear, date.getMonth, date.getDay)
    }

    TrafficDriver(
      id = lineItem.getId,
      name = lineItem.getName,
      url = s"https://www.google.com/dfp/$dfpNetworkCode#delivery/LineItemDetail/lineItemId=${lineItem.getId}",
      status = lineItem.getStatus.getValue,
      startDate = mkLocalDate(lineItem.getStartDateTime),
      endDate = mkLocalDate(lineItem.getEndDateTime),
      summaryStats = PerformanceStats(
        impressions = Option(lineItem.getStats) map (_.getImpressionsDelivered.toInt) getOrElse 0,
        clicks = Option(lineItem.getStats) map (_.getClicksDelivered.toInt) getOrElse 0
      )
    )
  }
}

case class TrafficDriverGroup(
  groupName: String,
  startDate: LocalDate,
  endDate: LocalDate,
  summaryStats: PerformanceStats,
  trafficDriverUrls: Seq[String]
)

object TrafficDriverGroup {

  implicit val writes = Json.writes[TrafficDriverGroup]

  implicit object DateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  def fromTrafficDrivers(groupName: String, trafficDrivers: Seq[TrafficDriver]): Option[TrafficDriverGroup] = {
    if (trafficDrivers.isEmpty) None
    else Some(
      TrafficDriverGroup(
        groupName,
        startDate = trafficDrivers.map(_.startDate).min,
        endDate = trafficDrivers.map(_.endDate).max,
        summaryStats = PerformanceStats.sum(trafficDrivers.map(_.summaryStats)),
        trafficDriverUrls = trafficDrivers.map(_.url)
      )
    )
  }
}
