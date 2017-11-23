import {AuthedReqwest} from '../util/pandaReqwest';
import Reqwest from 'reqwest';

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

export function fetchLatestAnalytics() {
  return AuthedReqwest({
    url: '/api/v2/campaigns/latestAnalytics',
    method: 'get'
  });
}

async function fetchShareCounts(analytics) {
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

export function fetchLatestAnalyticsForCampaign(id) {
  const req = AuthedReqwest({
    url: '/api/v2/campaigns/' + id + '/latestAnalytics',
    method: 'get'
  });
  return req.then(fetchShareCounts);
}

export function fetchCampaignPageViews(id) {
  return AuthedReqwest({
    url: '/api/v2/campaigns/' + id + '/pageViews',
    method: 'get'
  });
}

export function fetchCampaignUniques(id) {
  return AuthedReqwest({
    url: '/api/v2/campaigns/' + id + '/uniques',
    method: 'get'
  });
}

export function fetchCampaignTargetsReport(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/targetsReport',
    method: 'get'
  });
}

export function fetchCampaignContent(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/content',
    method: 'get'
  });
}

export function fetchCampaignReferrals(id) {
  return AuthedReqwest({
    url: '/api/v2/campaigns/' + id + '/referrals',
    method: 'get'
  });
}

export function refreshCampaignFromCAPI(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/refreshFromCAPI',
    method: 'post'
  });
}

export function getCampaignBenchmarks() {
  return AuthedReqwest({
    url: '/api/v2/campaigns/benchmarks',
    method: 'get'
  });
}

export function fetchCampaignMediaEvents(id) {
  return AuthedReqwest({
    url: '/api/campaigns/' + id + '/mediaEvents',
    method: 'get'
  });
}
