import {fetchOnPlatformReferrals} from '../../services/CampaignsApi';

function requestOnPlatformReferrals(id) {
   return {
        type:       'PLATFORM_REFERRALS_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveOnPlatformReferrals(referrals) {
    return {
        type:                'PLATFORM_REFERRALS_GET_RECEIVE',
        onPlatformReferrals: referrals,
        receivedAt:          Date.now()
    };
}

function errorReceivingOnPlatformReferrals(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get on-platform referrals',
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

export function getOnPlatformReferrals(id, startDate, endDate) {
    return dispatch => {
      dispatch(requestOnPlatformReferrals(id));
      return fetchOnPlatformReferrals(id, startDate, endDate)
        .catch(error => dispatch(errorReceivingOnPlatformReferrals(error)))
        .then(res => {
          dispatch(receiveOnPlatformReferrals(res));
        });
    };
}
