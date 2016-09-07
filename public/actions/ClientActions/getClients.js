import {fetchClients} from '../../services/ClientsApi';

function requestClients() {
    return {
        type:       'CLIENTS_GET_REQUEST',
        receivedAt: Date.now()
    };
}

function recieveClients(clients) {
    return {
        type:        'CLIENTS_GET_RECIEVE',
        clients:     clients,
        receivedAt:  Date.now()
    };
}

function errorRecievingClients(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get clients',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getClients() {
    return dispatch => {
      dispatch(requestClients());
      return fetchClients()
        .catch(error => dispatch(errorRecievingClients(error)))
        .then(res => {
          dispatch(recieveClients(res));
        });
    };
}
