import {AuthedReqwest} from '../util/pandaReqwest';

export function fetchCampaigns(territory) {
  return AuthedReqwest({
    url: `/api/campaigns?territory=${territory}`,
    method: 'get'
  });
}

export function fetchCampaign(id) {
  return AuthedReqwest({
    url: `/api/campaigns/${id}`,
    method: 'get'
  });
}

export function saveCampaign(id, campaign) {
  return AuthedReqwest({
    url: `/api/campaigns/${id}`,
    data: JSON.stringify(campaign),
    contentType: 'application/json',
    method: 'put'
  });
}

export function deleteCampaign(id) {
  return AuthedReqwest({
    url: `/api/campaigns/${id}`,
    contentType: 'application/json',
    method: 'delete'
  });
}

export function fetchLatestAnalytics(territory) {
  return AuthedReqwest({
    url: `/api/v2/campaigns/latestAnalytics?territory=${territory}`,
    method: 'get'
  });
}

export function fetchLatestAnalyticsForCampaign(id, territory) {
  return AuthedReqwest({
    url: `/api/v2/campaigns/${id}/latestAnalytics?territory=${territory}`,
    method: 'get'
  });
}

export function fetchCampaignPageViews(id) {
  return AuthedReqwest({
    url: `/api/v2/campaigns/${id}/pageViews`,
    method: 'get'
  });
}

export function fetchCampaignUniques(id) {
  return AuthedReqwest({
    url: `/api/v2/campaigns/${id}/uniques`,
    method: 'get'
  });
}

export function fetchCampaignTargetsReport(id) {
  return AuthedReqwest({
    url: `/api/campaigns/${id}/targetsReport`,
    method: 'get'
  });
}

export function fetchCampaignContent(id) {
  return AuthedReqwest({
    url: `/api/campaigns/${id}/content`,
    method: 'get'
  });
}

export function fetchCampaignReferrals(id) {
  return AuthedReqwest({
    url: `/api/v2/campaigns/${id}/referrals`,
    method: 'get'
  });
}

export function refreshCampaignFromCAPI(id) {
  return AuthedReqwest({
    url: `/api/campaigns/${id}/refreshFromCAPI`,
    method: 'post'
  });
}

export function getCampaignBenchmarks(territory) {
  return AuthedReqwest({
    url: `/api/v2/campaigns/benchmarks?territory=${territory}`,
    method: 'get'
  });
}

export function fetchCampaignMediaEvents(id) {
  return AuthedReqwest({
    url: `/api/campaigns/${id}/mediaEvents`,
    method: 'get'
  });
}
