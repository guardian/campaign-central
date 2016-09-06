import {saveClient as saveClientApi} from '../../services/ClientsApi';

function requestClientSave(id, client) {
    return {
        type:       'CLIENT_SAVE_REQUEST',
        id:         id,
        client:     client,
        receivedAt: Date.now()
    };
}

function recieveClientSave(client) {
    return {
        type:        'CLIENT_SAVE_RECIEVE',
        client:    client,
        receivedAt:  Date.now()
    };
}

function errorSavingClient(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not save client',
        error:      error,
        receivedAt: Date.now()
    };
}

export function saveClient(id, client) {
    return dispatch => {
      dispatch(requestClientSave(id, client));
      return saveClientApi(id, client)
        .catch(error => dispatch(errorSavingClient(error)))
        .then(res => {
          dispatch(recieveClientSave(res));
        });
    };
}
