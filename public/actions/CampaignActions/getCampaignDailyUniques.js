import {fetchCampaignDailyUniques} from '../../services/CampaignsApi';

function requestCampaignDailyUniques(id) {
    return {
        type:       'CAMPAIGN_DAILY_UNIQUES_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignDailyUniques(campaignDailyUniques) {
    return {
        type:        'CAMPAIGN_DAILY_UNIQUES_GET_RECEIVE',
        campaignDailyUniques:    campaignDailyUniques,
        receivedAt:  Date.now()
    };
}

function errorRecievingCampaignDailyUniques(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaign daily uniques',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignDailyUniques(id) {
    return dispatch => {
      dispatch(requestCampaignDailyUniques(id));
      return fetchCampaignDailyUniques(id)
        .catch(error => dispatch(errorRecievingCampaignDailyUniques(error)))
        .then(res => {
          dispatch(receiveCampaignDailyUniques(res));
        });
    };
}
