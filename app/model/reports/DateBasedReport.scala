package model.reports

import org.joda.time.DateTime

object DateBasedReport {

  val GA_SWITCH_ON_DATE = new DateTime("2016-07-01")

  def calculateDatesToFetch(startDate: DateTime, campaignEndDate: Option[DateTime]): List[DateTime] = {

    val sd = if(startDate.isBefore(GA_SWITCH_ON_DATE)) GA_SWITCH_ON_DATE else startDate
    val endDate = campaignEndDate.map(_.plusDays(1)).find(_.isBeforeNow) getOrElse(DateTime.now)

    var date = sd.withTimeAtStartOfDay()
    val endDay = endDate.withTimeAtStartOfDay()

    var daysInRange: List[DateTime] = Nil

    while (date.isBefore(endDay)) {
      daysInRange = daysInRange :+ date
      date = date.plusDays(1)
    }

    daysInRange
  }
}
