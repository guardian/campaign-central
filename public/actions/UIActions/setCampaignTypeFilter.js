export const SET_CAMPAIGN_TYPE_FILTER = 'SET_CAMPAIGN_TYPE_FILTER';

export function setCampaignTypeFilter(campaignTypeFilter) {
    return {
        type:       SET_CAMPAIGN_TYPE_FILTER,
        campaignTypeFilter:    campaignTypeFilter,
        receivedAt: Date.now()
    };
}
