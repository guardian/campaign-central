export const SET_CAMPAIGN_STATE_FILTER = 'SET_CAMPAIGN_STATE_FILTER';

export function setCampaignStateFilter(campaignStateFilter) {
    return {
        type:       SET_CAMPAIGN_STATE_FILTER,
        campaignStateFilter:    campaignStateFilter,
        receivedAt: Date.now()
    };
}
