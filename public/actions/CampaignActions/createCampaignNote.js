import {createCampaignNote} from '../../services/CampaignsApi';

function requestNoteCreate(note) {
    return {
        type: 'NOTE_CREATE_REQUEST',
        receivedAt: Date.now()
    };
}

function receiveNoteCreate(note) {
    return {
        type: 'NOTE_CREATE_RECIEVE',
        note: note,
        receivedAt: Date.now()
    };
}

function errorNoteCreate(error) {
    return {
        type: 'NOTE_CREATE_ERROR',
        message: 'Could not create note',
        error: error,
        receivedAt: Date.now()
    };
}

export function createNote(id, note) {
    return dispatch => {
        dispatch(requestNoteCreate(note));
        return createCampaignNote(id, note)
        .catch(error => dispatch(errorNoteCreate(error)))
        .then(res => {
            dispatch(receiveNoteCreate(res));
        });
    };
}
