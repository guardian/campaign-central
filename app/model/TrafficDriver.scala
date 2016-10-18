package model

import java.time.LocalDate

import com.google.api.ads.dfp.axis.v201608.{DateTime, LineItem}
import play.api.libs.json.Json
import services.Config.conf._

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
  ctrDelivered: Double
)

object TrafficDriver {

  implicit val trafficDriverWrites = Json.writes[TrafficDriver]

  def fromDfpLineItem(driverType: String)(lineItem: LineItem): TrafficDriver = {

    def mkLocalDate(dfpDateTime: DateTime): LocalDate = {
      val date = dfpDateTime.getDate
      LocalDate.of(date.getYear, date.getMonth, date.getDay)
    }

    TrafficDriver(
      id = lineItem.getId,
      name = lineItem.getName,
      url = s"https://www.google.com/dfp/$dfpNetworkCode#delivery/LineItemDetail/lineItemId=${lineItem.getId}",
      driverType,
      status = lineItem.getStatus.getValue,
      startDate = mkLocalDate(lineItem.getStartDateTime),
      endDate = mkLocalDate(lineItem.getEndDateTime),
      impressionsDelivered = lineItem.getStats.getImpressionsDelivered.toInt,
      clicksDelivered = lineItem.getStats.getClicksDelivered.toInt,
      ctrDelivered = lineItem.getStats.getClicksDelivered / lineItem.getStats.getImpressionsDelivered.toDouble * 100
    )
  }
}
