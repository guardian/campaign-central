import {fetchCampaignUniques} from '../../services/CampaignsApi';

function requestCampaignUniques(id) {
    return {
        type:       'CAMPAIGN_UNIQUES_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignUniques(campaignUniques) {
    return {
        type:        'CAMPAIGN_UNIQUES_GET_RECEIVE',
        campaignUniques:    campaignUniques,
        receivedAt:  Date.now()
    };
}

function errorReceivingCampaignUniques(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaign uniques',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignUniques(id, territory) {
    return dispatch => {
      dispatch(requestCampaignUniques(id));
      return fetchCampaignUniques(id, territory)
        .catch(error => dispatch(errorReceivingCampaignUniques(error)))
        .then(res => {
          dispatch(receiveCampaignUniques(res));
        });
    };
}
