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

export function deleteCampaign(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id,
    contentType: 'application/json',
    method: 'delete'
  });
}

export function fetchCampaignContent(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/content',
    contentType: 'application/json',
    method: 'get'
  });
}

export function fetchCampaignTrafficDrivers(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/drivers',
    contentType: 'application/json',
    method: 'get'
  });
}

export function fetchCampaignTrafficDriverSuggestions(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/suggest-drivers',
    contentType: 'application/json',
    method: 'get'
  });
}

export function acceptSuggestedCampaignTrafficDriver(campaignId, trafficDriverId) {
  return AuthedReqwest({
    url: '/api/campaigns/' + campaignId + '/driver/' + trafficDriverId,
    contentType: 'application/json',
    data: {},
    method: 'put'
  });
}

export function rejectSuggestedCampaignTrafficDriver(campaignId, trafficDriverId) {
  return AuthedReqwest({
    url: '/api/campaigns/' + campaignId + '/not-driver/' + trafficDriverId,
    contentType: 'application/json',
    data: {},
    method: 'put'
  });
}

export function fetchCampaignTrafficDriverStats(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/driverstats',
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

export function importCampaignFromTag(tag) {
  return AuthedReqwest({
    url: '/api/campaigns/import',
    data: JSON.stringify(tag),
    contentType: 'application/json',
    method: 'post'
  });
}

export function refreshCampaignFromCAPI(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/refreshFromCAPI',
    method: 'post'
  });
}
