import {combineReducers} from 'redux';
import error from './errorReducer';
import config from './configReducer';
import campaigns from './campaignsReducer';
import campaign from './campaignReducer';
import campaignPageViews from './campaignPageViewsReducer';
import campaignUniques from './campaignUniquesReducer';
import campaignTargetsReport from './campaignTargetsReportReducer';
import campaignCtaStats from './campaignCtaStatsReducer';
import campaignContent from './campaignContentReducer';
import onPlatformReferrals from './onPlatformReferralReducer';
import socialReferrals from './socialReferralReducer';
import campaignSort from './campaignSortReducer';
import latestAnalytics from './latestAnalyticsReducer';
import latestAnalyticsForCampaign from './latestAnalyticsForCampaignReducer';
import benchmarks from './allCampaignBenchmarksReducer';
import campaignMediaEvents from './campaignMediaEventsReducer';
import territory from './setTerritoryReducer';
import lastExecuted from './lastExecutedReducer';

export default combineReducers({
  error,
  config,
  campaigns,
  campaign,
  campaignPageViews,
  campaignUniques,
  campaignTargetsReport,
  campaignCtaStats,
  campaignContent,
  onPlatformReferrals,
  socialReferrals,
  campaignSort,
  latestAnalytics,
  latestAnalyticsForCampaign,
  benchmarks,
  campaignMediaEvents,
  territory,
  lastExecuted
});
