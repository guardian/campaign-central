package services

import model.{CampaignPageViewsItem, CampaignUniquesItem, GraphDataPoint, LatestCampaignAnalytics}
import org.joda.time.DateTime
import repositories.{
  CampaignPageViewsRepository,
  CampaignRepository,
  CampaignUniquesRepository,
  LatestCampaignAnalyticsRepository
}

class CampaignService(
  campaignRepository: CampaignRepository,
  campaignPageViewsRepository: CampaignPageViewsRepository,
  latestCampaignAnalyticsRepository: LatestCampaignAnalyticsRepository,
  campaignUniquesRepository: CampaignUniquesRepository
) {

  object DeviceTypes {
    val MobileDeviceTypes = Set(
      "PDA",
      "SMARTPHONE",
      "TABLET",
      "WEARABLE_COMPUTER",
      "GUARDIAN_ANDROID_NATIVE_APP",
      "GUARDIAN_IOS_NATIVE_APP",
      "GUARDIAN_WINDOWS_APP"
    )
    val DesktopDeviceTypes  = Set("GAME_CONSOLE", "PERSONAL_COMPUTER", "SMART_TV")
    val ExcludedDeviceTypes = Set("UNKNOWN", "OTHER")
  }

  def getPageViews(campaignId: String): Seq[CampaignPageViewsItem] = {
    campaignPageViewsRepository.getCampaignPageViews(campaignId)
  }

  def getUniques(campaignId: String): Seq[CampaignUniquesItem] = {
    campaignUniquesRepository.getCampaignUniques(campaignId)
  }

  def getLatestAnalyticsForCampaign(campaignId: String): Option[LatestCampaignAnalytics] = {
    for {
      campaign <- campaignRepository.getCampaign(campaignId)
      latest   <- latestCampaignAnalyticsRepository.getLatestCampaignAnalytics(campaignId)
    } yield {
      val uniquesDeviceBreakdown = breakdownUniquesByMobileAndDesktop(latest.uniques, latest.uniquesByDevice)
      val uniquesTarget: Long    = campaign.targets.getOrElse("uniques", 0)
      LatestCampaignAnalytics(
        latest.campaignId,
        latest.uniques,
        uniquesDeviceBreakdown.mobile,
        uniquesDeviceBreakdown.desktop,
        uniquesTarget,
        latest.pageviews,
        latest.medianAttentionTimeSeconds,
        latest.medianAttentionTimeByDevice.map(normaliseDeviceData)
      )

    }
  }

  def getLatestCampaignAnalytics(): Map[String, LatestCampaignAnalytics] = {
    val latestCampaignAnalytics = latestCampaignAnalyticsRepository.getLatestCampaignAnalytics()
    val campaignsWeHaveUniquesFor =
      campaignRepository.getAllCampaigns().filter(c => latestCampaignAnalytics.map(_.campaignId).contains(c.id))

    val results = campaignsWeHaveUniquesFor flatMap { campaign =>
      for {
        latest <- latestCampaignAnalytics.find(_.campaignId == campaign.id)
      } yield {
        val uniquesDeviceBreakdown = breakdownUniquesByMobileAndDesktop(latest.uniques, latest.uniquesByDevice)
        val uniquesTarget: Long    = campaign.targets.getOrElse("uniques", 0)
        campaign.id -> LatestCampaignAnalytics(
          latest.campaignId,
          latest.uniques,
          uniquesDeviceBreakdown.mobile,
          uniquesDeviceBreakdown.desktop,
          uniquesTarget,
          latest.pageviews,
          latest.medianAttentionTimeSeconds,
          latest.medianAttentionTimeByDevice.map(normaliseDeviceData)
        )
      }
    }

    results.toMap
  }

  def getUniquesDataForGraph(campaignId: String): Option[Seq[GraphDataPoint]] = {

    val campaignUniques = campaignUniquesRepository.getCampaignUniques(campaignId)
    val initialDataPoint = campaignUniques.headOption.map { item =>
      item.copy(reportExecutionTimestamp = new DateTime(item.reportExecutionTimestamp).minusDays(1).toString,
                uniques = 0L)
    }

    val uniqueItems = initialDataPoint ++ campaignUniques
    val maybeTarget = campaignRepository.getCampaign(campaignId).flatMap(_.targets.get("uniques"))

    maybeTarget map { target =>
      val runRateStep = {
        val numItems = uniqueItems.size.toLong
        if (numItems == 0) 1
        else target / numItems
      }
      val runRate = Seq.range[Long](0, target + runRateStep, runRateStep)
      (uniqueItems zip runRate).map {
        case (unique, rate) =>
          GraphDataPoint(
            name = unique.reportExecutionTimestamp,
            dataPoint = unique.uniques,
            target = rate
          )
      }.toSeq
    }
  }

  case class DeviceBreakdown(mobile: Long, desktop: Long)

  private def breakdownUniquesByMobileAndDesktop(totalUniques: Long,
                                                 uniquesPerDevice: Map[String, Long]): DeviceBreakdown = {
    val uniquesFromOther: Long =
      uniquesPerDevice.filter { case (key, _) => DeviceTypes.ExcludedDeviceTypes.contains(key) }.values.sum
    val uniquesByDeviceWithoutExcludes = uniquesPerDevice -- DeviceTypes.ExcludedDeviceTypes
    val (mobile, _) = uniquesByDeviceWithoutExcludes.partition {
      case (key, _) => DeviceTypes.MobileDeviceTypes.contains(key)
    }

    val uniquesFromMobile  = mobile.values.sum + (if (uniquesFromOther > 0) uniquesFromOther / 2 else 0)
    val uniquesFromDesktop = totalUniques - uniquesFromMobile

    DeviceBreakdown(uniquesFromMobile, uniquesFromDesktop)
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
