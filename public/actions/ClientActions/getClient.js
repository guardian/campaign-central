import {fetchClient} from '../../services/ClientsApi';

function requestClient(id) {
    return {
        type:       'CLIENT_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function recieveClient(client) {
    return {
        type:        'CLIENT_GET_RECIEVE',
        client:      client,
        receivedAt:  Date.now()
    };
}

function errorRecievingClient(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get client',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getClient(id) {
    return dispatch => {
      dispatch(requestClient(id));
      return fetchClient(id)
        .catch(error => dispatch(errorRecievingClient(error)))
        .then(res => {
          dispatch(recieveClient(res));
        });
    };
}
