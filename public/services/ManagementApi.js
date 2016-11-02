import {AuthedReqwest} from '../util/pandaReqwest';

export function fetchAnalyticsCacheSummary() {
  return AuthedReqwest({
    url: '/management/api/analytics',
    contentType: 'application/json',
    method: 'get'
  });
}

export function refreshItem(item) {
  return AuthedReqwest({
    url: '/management/api/analytics/' + item.dataType + '/' + item.key,
    contentType: 'application/json',
    method: 'post',
    data: JSON.stringify(item)
  });
}

export function deleteItem(item) {
  return AuthedReqwest({
    url: '/management/api/analytics/' + item.dataType + '/' + item.key,
    contentType: 'application/json',
    method: 'delete'
  });
}
