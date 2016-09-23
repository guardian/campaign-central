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

export function fetchCampaignAnalytics(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/analytics',
    contentType: 'application/json',
    method: 'get'
  });
}

export function fetchCampaignNotes(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/notes',
    contentType: 'application/json',
    method: 'get'
  });
}

export function createCampaignNote(id, note) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/notes',
    data: JSON.stringify(note),
    contentType: 'application/json',
    method: 'post'
  });
}

export function updateCampaignNote(id, date, note) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/note/' + date,
    data: JSON.stringify(note),
    contentType: 'application/json',
    method: 'put'
  });
}
