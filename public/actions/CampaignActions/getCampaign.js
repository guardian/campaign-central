import {fetchCampaign} from '../../services/CampaignsApi';

function requestCampaign(id) {
    return {
        type:       'CAMPAIGN_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaign(campaign) {
    return {
        type:        'CAMPAIGN_GET_RECEIVE',
        campaign:    campaign,
        receivedAt:  Date.now()
    };
}

function errorRecievingCampaign(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaign',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaign(id) {
    return dispatch => {
      dispatch(requestCampaign(id));
      return fetchCampaign(id)
        .catch(error => dispatch(errorRecievingCampaign(error)))
        .then(res => {
          dispatch(receiveCampaign(res));
        });
    };
}
