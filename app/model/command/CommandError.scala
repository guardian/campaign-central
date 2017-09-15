package model.command

import model.{Campaign, ContentItem}

sealed trait CampaignCentralApiError
case class CampaignNotFound(message: String)                                          extends CampaignCentralApiError
case class LatestCampaignAnalyticsItemNotFound(message: String)                       extends CampaignCentralApiError
case class ContentItemNotFound(message: String)                                       extends CampaignCentralApiError
case class SponsorshipNotFound(message: String)                                       extends CampaignCentralApiError
case class ContentItemFailedToPersist(contentItem: ContentItem, exception: Throwable) extends CampaignCentralApiError
case class CampaignDeletionFailed(campaignId: String, exception: Throwable)           extends CampaignCentralApiError
case class CampaignItemDeletionFailed(campaignId: String, exception: Throwable)       extends CampaignCentralApiError
case class CampaignPutError(campaign: Campaign, exception: Throwable)                 extends CampaignCentralApiError
case class CampaignTagNotFound(id: Long, externalName: String)                        extends CampaignCentralApiError
case object InvalidCampaignTagType                                                    extends CampaignCentralApiError
case class CampaignMissingPathPrefix(campaign: Campaign)                              extends CampaignCentralApiError
case class JsonParsingError(message: String)                                          extends CampaignCentralApiError
case class FetchTagSponsorshipFailed(message: String)                                 extends CampaignCentralApiError
