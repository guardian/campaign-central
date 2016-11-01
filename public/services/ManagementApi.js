import {AuthedReqwest} from '../util/pandaReqwest';

export function fetchAnalyticsCacheSummary() {
  return AuthedReqwest({
    url: '/management/api/analytics',
    contentType: 'application/json',
    method: 'get'
  });
}
