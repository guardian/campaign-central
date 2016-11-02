import {fetchCampaignNotes} from '../../services/CampaignsApi';

function requestCampaignNotes(id) {
   return {
        type:       'NOTES_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignNotes(notes) {
    return {
        type:             'NOTES_GET_RECEIVE',
        campaignNotes:    notes,
        receivedAt:       Date.now()
    };
}

function errorRecievingCampaignNotes(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get notes',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignNotes(id) {
    return dispatch => {
      dispatch(requestCampaignNotes(id));
      return fetchCampaignNotes(id)
        .catch(error => dispatch(errorRecievingCampaignNotes(error)))
        .then(res => {
          dispatch(receiveCampaignNotes(res));
        });
    };
}
