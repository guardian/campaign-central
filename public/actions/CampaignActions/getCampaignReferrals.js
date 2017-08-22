import {fetchCampaignReferrals} from '../../services/CampaignsApi';

function requestCampaignReferrals(id) {
   return {
        type:       'REFERRALS_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignReferrals(referrals) {
    return {
        type:              'REFERRALS_GET_RECEIVE',
        campaignReferrals: referrals,
        receivedAt:        Date.now()
    };
}

function errorReceivingCampaignReferrals(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get referrals',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignReferrals(id) {
    return dispatch => {
      dispatch(requestCampaignReferrals(id));
      return fetchCampaignReferrals(id)
        .catch(error => dispatch(errorReceivingCampaignReferrals(error)))
        .then(res => {
          dispatch(receiveCampaignReferrals(res));
        });
    };
}
