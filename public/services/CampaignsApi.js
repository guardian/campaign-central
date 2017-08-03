import {AuthedReqwest} from '../util/pandaReqwest';

export function fetchCampaigns() {
  return AuthedReqwest({
    url: '/api/campaigns',
    method: 'get'
  });
}

export function fetchCampaign(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id,
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

export function fetchOverallAnalyticsSummary() {
  return AuthedReqwest({
    url: '/api/campaigns/analytics',
    method: 'get'
  });
}

export function fetchCampaignPageViews(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/pageViews',
    method: 'get'
  });
}

export function fetchCampaignDailyUniques(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/dailyUniques',
    method: 'get'
  });
}

export function fetchCampaignTargetsReport(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/targetsReport',
    method: 'get'
  });
}

export function fetchCampaignQualifiedReport(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/qualifiedReport',
    method: 'get'
  });
}

export function fetchCampaignContent(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/content',
    method: 'get'
  });
}

export function fetchCampaignTrafficDrivers(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/drivers',
    method: 'get'
  });
}

export function fetchCampaignTrafficDriverSuggestions(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/suggest-drivers',
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
    method: 'get'
  });
}

export function fetchCampaignCtaStats(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/ctastats',
    method: 'get'
  });
}

export function fetchCampaignNotes(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/notes',
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
