import {AuthedReqwest} from '../util/pandaReqwest';

export function fetchCampaigns() {
  return AuthedReqwest({
    url: '/api/campaigns',
    contentType: 'application/json',
    method: 'get'
  });
}

export function fetchCampaign(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id,
    contentType: 'application/json',
    method: 'get'
  });
}

export function fetchCampaignAnalytics(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/analytics',
    contentType: 'application/json',
    method: 'get'
  });
}
