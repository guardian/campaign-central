import {updateCampaignNote} from '../../services/CampaignsApi';

function requestNoteUpdate(note) {
    return {
        type: 'NOTE_UPDATE_REQUEST',
        receivedAt: Date.now()
    };
}

function receiveNoteUpdate(note) {
    return {
        type: 'NOTE_UPDATE_RECEIVE',
        note: note,
        receivedAt: Date.now()
    };
}

function errorNoteUpdate(error) {
    return {
        type: 'NOTE_UPDATE_ERROR',
        message: 'Could not create note',
        error: error,
        receivedAt: Date.now()
    };
}

export function updateNote(id, note) {
    return dispatch => {
        dispatch(requestNoteUpdate(note));
        return updateCampaignNote(id, note)
        .catch(error => dispatch(errorNoteUpdate(error)))
        .then(res => {
            dispatch(receiveNoteUpdate(res));
        });
    };
}

