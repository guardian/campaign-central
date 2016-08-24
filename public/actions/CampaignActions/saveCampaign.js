import {saveCampaign as saveCampaignApi} from '../../services/CampaignsApi';

function requestCampaignSave(id, campaign) {
    return {
        type:       'CAMPAIGN_SAVE_REQUEST',
        id:         id,
        campaign:   campaign,
        receivedAt: Date.now()
    };
}

function recieveCampaignSave(campaign) {
    return {
        type:        'CAMPAIGN_SAVE_RECIEVE',
        campaign:    campaign,
        receivedAt:  Date.now()
    };
}

function errorSavingCampaign(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not save campaign',
        error:      error,
        receivedAt: Date.now()
    };
}

export function saveCampaign(id, campaign) {
    return dispatch => {
      dispatch(requestCampaignSave(id, campaign));
      return saveCampaignApi(id, campaign)
        .catch(error => dispatch(errorSavingCampaign(error)))
        .then(res => {
          dispatch(recieveCampaignSave(res));
        });
    };
}
