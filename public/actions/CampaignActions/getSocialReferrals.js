import {fetchSocialReferrals} from '../../services/CampaignsApi';

function requestSocialReferrals(id) {
  return {
    type:       'SOCIAL_REFERRALS_GET_REQUEST',
    id:         id,
    receivedAt: Date.now()
  };
}

function receiveSocialReferrals(referrals) {
  return {
    type:            'SOCIAL_REFERRALS_GET_RECEIVE',
    socialReferrals: referrals,
    receivedAt:      Date.now()
  };
}

function errorReceivingSocialReferrals(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get social referrals',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getSocialReferrals(id, startDate, endDate) {
  return dispatch => {
    dispatch(requestSocialReferrals(id));
    return fetchSocialReferrals(id, startDate, endDate)
      .catch(error => dispatch(errorReceivingSocialReferrals(error)))
      .then(res => {
        dispatch(receiveSocialReferrals(res));
      });
  };
}
