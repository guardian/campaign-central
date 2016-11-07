import {deleteCampaign as deleteCampaignApi} from '../../services/CampaignsApi';


function requestCampaignDelete(id, campaign) {
  return {
    type:       'CAMPAIGN_DELETE_REQUEST',
    id:         id,
    campaign:   campaign,
    receivedAt: Date.now()
  };
}

function receiveCampaignDelete(campaign) {
  return {
    type:        'CAMPAIGN_DELETE_RECEIVE',
    campaign:    campaign,
    receivedAt:  Date.now()
  };
}

function errorDeletingCampaign(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not delete campaign',
    error:      error,
    receivedAt: Date.now()
  };
}

export function deleteCampaign(id) {
  return dispatch => {
    dispatch(requestCampaignDelete(id));
    return deleteCampaignApi(id)
      .catch(error => dispatch(errorDeletingCampaign(error)))
      .then(res => {
        dispatch(receiveCampaignDelete(res));
      });
  };
}
