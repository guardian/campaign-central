import {AuthedReqwest} from '../util/pandaReqwest';
import Reqwest from 'reqwest';

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

async function fetchShareCounts(analytics, territory) {
  if (territory !== 'global') {
    return analytics;
  }
  const paths = Object.entries(analytics.analyticsByPath || {});

  // Facebook shares should be fetched sequentially to avoid rate-limit.
  for (const [key, values] of paths) {
    const req = {
      url: `https://graph.facebook.com/?id=https://theguardian.com${key}`,
      method: 'get'
    };
    const fbRateLimit = new Promise(function(resolve) {
      Reqwest(req)
        .then(res => {
          values.facebookShares = res.share ? res.share.share_count : 0;
          resolve();
        })
        .fail(resolve);
    });
    await fbRateLimit;
  }
  // LinkedIn shares should be fetched in parallel.
  const linkedInShares = paths.map( ([key, values]) => {
    const req = {
      url: `https://www.linkedin.com/countserv/count/share?url=https://theguardian.com${key}`,
      type: 'jsonp'
    };
    return new Promise(function(resolve) {
      Reqwest(req)
        .then(res => {
          values.linkedInShares = res.count;
          resolve();
        })
        .fail(resolve);
    });
  });
  return Promise.all(linkedInShares)
    .then(() => analytics)
    .catch(() => analytics);
}

export function fetchLatestAnalyticsForCampaign(id, territory) {
  const req = AuthedReqwest({
    url: `/api/v2/campaigns/${id}/latestAnalytics?territory=${territory}`,
    method: 'get'
  });
  return req.then( (analytics) => fetchShareCounts(analytics, territory));
}

export function fetchCampaignPageViews(id) {
  return AuthedReqwest({
    url: `/api/v2/campaigns/${id}/pageViews`,
    method: 'get'
  });
}

export function fetchCampaignUniques(id, territory) {
  return AuthedReqwest({
    url: `/api/v2/campaigns/${id}/uniques?territory=${territory}`,
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

export function fetchCampaignReferrals(id, dateRange) {
  return AuthedReqwest({
    url: `/api/v2/campaigns/${id}/platform-referrals?start=${dateRange.startDate}&end=${dateRange.endDate}`,
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
