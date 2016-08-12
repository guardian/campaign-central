import {AuthedReqwest} from '../util/pandaReqwest';

export function fetchCampaigns() {
  return AuthedReqwest({
    url: '/api/campaigns',
    contentType: 'application/json',
    method: 'get'
  });
}
