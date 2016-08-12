import {fetchCampaigns} from '../../services/CampaignsApi';

function requestCampaigns() {
    return {
        type:       'CAMPAIGNS_GET_REQUEST',
        receivedAt: Date.now()
    };
}

function recieveCampaigns(campaigns) {
    return {
        type:        'CAMPAIGNS_GET_RECIEVE',
        campaigns:   campaigns,
        receivedAt:  Date.now()
    };
}

function errorRecievingCampaigns(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaigns',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaigns() {
    return dispatch => {
      dispatch(requestCampaigns());
      return fetchCampaigns()
        .catch(error => dispatch(errorRecievingCampaigns(error)))
        .then(res => {
          dispatch(recieveCampaigns(res));
        });
    };
}
