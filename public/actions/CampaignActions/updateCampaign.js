export function updateCampaign(id, campaign) {
    return {
        type:       'CAMPAIGN_UPDATE_REQUEST',
        id:         id,
        campaign:   campaign,
        receivedAt: Date.now()
    };
}
