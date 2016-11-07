import {fetchCampaignContent} from '../../services/CampaignsApi';

function requestCampaignContent(id) {
   return {
        type:       'CONTENT_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignContent(content) {
    return {
        type:             'CONTENT_GET_RECEIVE',
        campaignContent:    content,
        receivedAt:       Date.now()
    };
}

function errorRecievingCampaignContent(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get content',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignContent(id) {
    return dispatch => {
      dispatch(requestCampaignContent(id));
      return fetchCampaignContent(id)
        .catch(error => dispatch(errorRecievingCampaignContent(error)))
        .then(res => {
          dispatch(receiveCampaignContent(res));
        });
    };
}
