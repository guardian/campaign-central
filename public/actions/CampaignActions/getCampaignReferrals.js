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

function toggleOrder(field) {
    return {
        type:       'REFERRALS_TOGGLE_ORDER',
        field,
        receivedAt: Date.now()
    };
}

export function toggleNode() {
    return {
        type:       'REFERRALS_TOGGLE_NODE',
        receivedAt: Date.now()
    }
}

export function setToggleNode() {
    return dispatch => dispatch(toggleNode());
}

export function setToggleOrder(field) {
    return dispatch => dispatch(toggleOrder(field));
}

export function getCampaignReferrals(id, startDate, endDate) {
    return dispatch => {
      dispatch(requestCampaignReferrals(id));
      return fetchCampaignReferrals(id, startDate, endDate)
        .catch(error => dispatch(errorReceivingCampaignReferrals(error)))
        .then(res => {
          dispatch(receiveCampaignReferrals(res));
        });
    };
}
