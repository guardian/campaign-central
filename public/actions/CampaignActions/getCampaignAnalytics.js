import {fetchCampaignAnalytics} from '../../services/CampaignsApi';

function requestCampaignAnalytics(id) {
    return {
        type:       'CAMPAIGN_ANALYTICS_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignAnalytics(campaignAnalytics) {
    return {
        type:        'CAMPAIGN_ANALYTICS_GET_RECEIVE',
        campaignAnalytics:    campaignAnalytics,
        receivedAt:  Date.now()
    };
}

function errorRecievingCampaignAnalytics(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaign analytics',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignAnalytics(id) {
    return dispatch => {
      dispatch(requestCampaignAnalytics(id));
      return fetchCampaignAnalytics(id)
        .catch(error => dispatch(errorRecievingCampaignAnalytics(error)))
        .then(res => {
          dispatch(receiveCampaignAnalytics(res));
        });
    };
}
