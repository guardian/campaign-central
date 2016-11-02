
function receiveCampaignAnalytics() {
    return {
        type:        'CAMPAIGN_ANALYTICS_GET_RECEIVE',
        campaignAnalytics:    undefined,
        receivedAt:  Date.now()
    };
}

export function clearCampaignAnalytics() {
    return dispatch => {
        dispatch(receiveCampaignAnalytics());
    };
}
