import {fetchLastExecuted} from '../../services/CampaignsApi';

function requestLastExecuted() {
    return {
        type:       'LAST_EXECUTED_GET_REQUEST',
        receivedAt: Date.now()
    };
}

function receiveLastExecuted(lastExecuted) {
    return {
        type:        'LAST_EXECUTED_GET_RECEIVE',
        lastExecuted:    lastExecuted,
        receivedAt:  Date.now()
    };
}

function errorRecievingLastExecuted(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get last executed report time.',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getLastExecuted(territory) {
    return dispatch => {
      dispatch(requestLastExecuted());
      return fetchLastExecuted(territory)
        .catch(error => dispatch(errorRecievingLastExecuted(error)))
        .then(res => {
          dispatch(receiveLastExecuted(res));
        });
    };
}
