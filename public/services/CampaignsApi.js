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

export function saveCampaign(id, campaign) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id,
    data: JSON.stringify(campaign),
    contentType: 'application/json',
    method: 'put'
  });
}