package model.reports

import model.Campaign
import org.joda.time.{DateTime, Duration}

object CampaignSummary {

  def calculateTargetToDate(campaign: Campaign, date: DateTime): Long = {
    val targetToDate = for {
      startDate <- campaign.startDate
      endDate <- campaign.endDate
      uniqueTarget <- campaign.targets.get("uniques")
    } yield {

      val campaignLength = new Duration(startDate, endDate).getStandardDays
      val daysIn = new Duration(startDate, date).getStandardDays

      val dailyRunRate = uniqueTarget.toDouble / campaignLength

      (daysIn * dailyRunRate).toLong
    }

    targetToDate.getOrElse(0L)
  }
}
