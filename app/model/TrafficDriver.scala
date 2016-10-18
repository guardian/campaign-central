package model

import java.time.LocalDate

import play.api.libs.json.Json

case class TrafficDriver(
  id: Long,
  name: String,
  url: String,
  driverType: String,
  status: String,
  startDate: LocalDate,
  endDate: LocalDate,
  impressionsDelivered: Int,
  clicksDelivered: Int,
  ctrDelivered: Double,
  expectedDeliveryPercentage: Option[Double],
  actualDeliveryPercentage: Option[Double]
)

object TrafficDriver {

  implicit val trafficDriverWrites = Json.writes[TrafficDriver]
}
