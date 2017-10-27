package services

import com.gu.contentapi.client.model.v1.{Content => CapiContent, Section => CapiSection}
import model._
import org.joda.time.DateTime
import play.api.Logger
import repositories._
import cats.implicits._
import repositories.contentapi.{CapiContentTransformer, ContentApi}

object CampaignService {

  object DeviceTypes {
    val MobileDeviceTypes = Set("PDA",
                                "SMARTPHONE",
                                "TABLET",
                                "WEARABLE_COMPUTER",
                                "GUARDIAN_ANDROID_NATIVE_APP",
                                "GUARDIAN_IOS_NATIVE_APP",
                                "GUARDIAN_WINDOWS_APP")
    val DesktopDeviceTypes  = Set("GAME_CONSOLE", "PERSONAL_COMPUTER", "SMART_TV")
    val ExcludedDeviceTypes = Set("UNKNOWN", "OTHER")
  }

  def getPageViews(campaignId: String): Either[CampaignCentralApiError, Seq[CampaignPageViewsItem]] = {
    CampaignPageViewsRepository.getCampaignPageViews(campaignId)
  }

  def getUniques(campaignId: String): Either[CampaignCentralApiError, Seq[CampaignUniquesItem]] = {
    CampaignUniquesRepository.getCampaignUniques(campaignId)
  }

  def getLatestAnalyticsForCampaign(campaignId: String): Either[CampaignCentralApiError, LatestCampaignAnalytics] = {
    for {
      campaign <- CampaignRepository.getCampaign(campaignId)
      latest   <- LatestCampaignAnalyticsRepository.getLatestCampaignAnalytics(campaignId)
    } yield {
      val uniquesTarget: Long = campaign.targets.getOrElse("uniques", 0)
      LatestCampaignAnalytics(latest, uniquesTarget)
    }
  }

  def getLatestCampaignAnalytics(): Either[CampaignCentralApiError, Map[String, LatestCampaignAnalytics]] = {

    for {
      latestCampaignAnalytics <- LatestCampaignAnalyticsRepository.getLatestCampaignAnalytics()
      campaignsWeHaveUniquesFor <- CampaignRepository
        .getAllCampaigns()
        .map(campaigns => campaigns.filter(c => latestCampaignAnalytics.map(_.campaignId).contains(c.id)))
    } yield {
      val results = campaignsWeHaveUniquesFor flatMap { campaign =>
        for {
          latest <- latestCampaignAnalytics.find(_.campaignId == campaign.id)
        } yield {
          val uniquesTarget: Long = campaign.targets.getOrElse("uniques", 0)
          campaign.id -> LatestCampaignAnalytics(latest, uniquesTarget)
        }
      }

      results.toMap
    }
  }

  def getBenchmarksAcrossCampaigns(): Either[CampaignCentralApiError, Benchmarks] = {
    for {
      latestCampaignAnalytics <- getLatestCampaignAnalytics()
      campaigns               <- CampaignRepository.getAllCampaigns()
    } yield {

      val paidForCampaignsIds = campaigns.filter(_.`type`.toLowerCase == "paidcontent").map(_.id)
      val hostedCampaignIds   = campaigns.filter(_.`type`.toLowerCase == "hosted").map(_.id)

      val allCampaignAnalytics     = latestCampaignAnalytics.values.toSeq
      val paidForCampaignAnalytics = paidForCampaignsIds.flatMap(latestCampaignAnalytics.get)
      val hostedCampaignAnalytics  = hostedCampaignIds.flatMap(latestCampaignAnalytics.get)

      Benchmarks(
        totals = Totals(allCampaignAnalytics),
        averages = Averages(allCampaignAnalytics),
        medians = Medians(allCampaignAnalytics),
        paidFor = PaidFor(
          totals = Totals(paidForCampaignAnalytics),
          averages = Averages(paidForCampaignAnalytics),
          medians = Medians(paidForCampaignAnalytics)
        ),
        hosted = Hosted(
          totals = Totals(hostedCampaignAnalytics),
          averages = Averages(hostedCampaignAnalytics),
          medians = Medians(hostedCampaignAnalytics)
        )
      )
    }
  }

  def getUniquesDataForGraph(campaignId: String): Either[CampaignCentralApiError, Option[Seq[GraphDataPoint]]] = {

    for {
      campaignUniques <- CampaignUniquesRepository.getCampaignUniques(campaignId)
      campaign        <- CampaignRepository.getCampaign(campaignId)
    } yield {
      val initialDataPoint = campaignUniques.headOption.map { item =>
        item.copy(reportExecutionTimestamp = new DateTime(item.reportExecutionTimestamp).minusDays(1).toString,
                  uniques = 0L)
      }

      val uniqueItems = initialDataPoint ++ campaignUniques
      val maybeTarget = campaign.targets.get("uniques")

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
  }

  def refreshCampaignById(campaignId: String)(implicit user: User): Either[CampaignCentralApiError, Campaign] = {
    CampaignRepository.getCampaign(campaignId) flatMap { campaign =>
      val sectionId = campaign.pathPrefix
      val content   = ContentApi.loadAllContentInSection(sectionId)

      val sectionOrError = ContentApi
        .getSection(sectionId)
        .map(Right(_))
        .getOrElse(Left(CampaignSectionNotFound(s"Could not find section with id: $sectionId")))

      for {
        section <- sectionOrError
        updatedCampaign = CampaignTransformer.updateExistingCampaign(section, campaign, user)
        _ <- updateCampaignContent(content, updatedCampaign)
        _ <- CampaignRepository.putCampaign(updatedCampaign)
      } yield {
        Logger.info(s"refreshing campaign ${updatedCampaign.name} (${updatedCampaign.id})")
        updatedCampaign
      }
    }
  }

  def synchroniseCampaigns()(implicit user: User): Either[CampaignCentralApiError, List[Campaign]] = {

    val sectionsToCreateOrUpdate: Seq[(Option[CapiSection], Option[Campaign])] = {
      val currentCampaigns = CampaignRepository
        .getAllCampaigns()
        .toOption
        .getOrElse(Nil)
        .map { campaign =>
          campaign.pathPrefix -> campaign
        }
        .toMap

      val sections = ContentApi.getSectionsWithPaidContentSponsorship()

      val existingCampaigns = sections map { section =>
        (Some(section), currentCampaigns.get(section.id))
      }

      val deadCampaigns: Seq[(Option[CapiSection], Option[Campaign])] = {
        val deadCampaignIds = (currentCampaigns.keys.toSet diff sections.map(_.id).toSet).toSeq
        deadCampaignIds.flatMap(currentCampaigns.get).map { deadCampaign =>
          (None, Some(deadCampaign))
        }
      }

      deadCampaigns ++ existingCampaigns
    }

    val campaigns: Seq[Campaign] = sectionsToCreateOrUpdate.flatMap {
      case (Some(section), Some(existingCampaign)) =>
        Some(CampaignTransformer.updateExistingCampaign(section, existingCampaign, user))

      case (None, Some(existingCampaign)) =>
        Some(CampaignTransformer.updateExistingCampaignThatsFinished(existingCampaign))

      case (Some(section), None) =>
        CampaignTransformer.createDefaultCampaign(section) orElse {
          Logger.warn(s"Could not create campaign from section: ${section.id}")
          None
        }

      case _ => None
    }

    val updatedCampaignsOrError: List[Either[CampaignCentralApiError, Campaign]] = campaigns.map { campaign =>
      val sectionId = campaign.pathPrefix
      val content   = ContentApi.loadAllContentInSection(sectionId)

      val sectionOrError = ContentApi
        .getSection(sectionId)
        .map(Right(_))
        .getOrElse(Left(CampaignSectionNotFound(s"Could not find section with id: $sectionId")))

      for {
        section <- sectionOrError
        _       <- updateCampaignContent(content, campaign)
        _       <- CampaignRepository.putCampaign(campaign)
      } yield {
        Logger.info(s"refreshing campaign ${campaign.name} (${campaign.id})")
        campaign
      }

    }.toList

    updatedCampaignsOrError.sequence
  }

  private def updateCampaignContent(apiContent: List[CapiContent],
                                    campaign: Campaign): Either[CampaignCentralApiError, Seq[PutContentItemResult]] = {

    val contentItems: List[ContentItem] = CapiContentTransformer.buildContentItems(apiContent, campaign.id)

    val putContentResults: List[Either[CampaignCentralApiError, PutContentItemResult]] = for {
      item <- contentItems
    } yield CampaignContentRepository.putContent(item)

    putContentResults.collectFirst {
      case Left(e) =>
        Logger.error(s"Failures putting content for campaign ${campaign.id} (${campaign.name}): $e")
        e
    } toLeft {
      putContentResults collect {
        case Right(result) => result
      }
    }

  }
}
