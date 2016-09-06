import {AuthedReqwest} from '../util/pandaReqwest';

export function fetchClients() {
  return AuthedReqwest({
    url: '/api/clients',
    contentType: 'application/json',
    method: 'get'
  });
}

export function fetchClient(id) {
  return AuthedReqwest({
    url: '/api/clients/' + id,
    contentType: 'application/json',
    method: 'get'
  });
}

export function saveClient(id, client) {
  return AuthedReqwest({
    url: '/api/clients/' + id,
    data: JSON.stringify(client),
    contentType: 'application/json',
    method: 'put'
  });
}
