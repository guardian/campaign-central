import { combineReducers } from 'redux';
import error from './errorReducer';
import config from './configReducer';
import campaigns from './campaignsReducer';
import campaign from './campaignReducer';
import campaignPageViews from './campaignPageViewsReducer';
import campaignUniques from './campaignUniquesReducer';
import campaignTargetsReport from './campaignTargetsReportReducer';
import campaignQualifiedReport from './campaignQualifiedReportReducer';
import campaignCtaStats from './campaignCtaStatsReducer';
import campaignContent from './campaignContentReducer';
import campaignReferrals from './campaignReferralReducer';
import campaignTrafficDrivers from './campaignTrafficDriverReducer';
import campaignTrafficDriversDirty from './campaignTrafficDriversDirtyReducer';
import campaignTrafficDriverSuggestions from './campaignTrafficDriverSuggestionsReducer';
import campaignTrafficDriverSuggestionsDirty from './campaignTrafficDriverSuggestionsDirtyReducer';
import campaignTrafficDriverStats from './campaignTrafficDriverStatsReducer';
import campaignSort from './campaignSortReducer';
import latestAnalytics from './latestAnalyticsReducer';
import latestAnalyticsForCampaign from './latestAnalyticsForCampaignReducer';

export default combineReducers({
  error,
  config,
  campaigns,
  campaign,
  campaignPageViews,
  campaignUniques,
  campaignTargetsReport,
  campaignQualifiedReport,
  campaignCtaStats,
  campaignContent,
  campaignReferrals,
  campaignTrafficDrivers,
  campaignTrafficDriversDirty,
  campaignTrafficDriverSuggestions,
  campaignTrafficDriverSuggestionsDirty,
  campaignTrafficDriverStats,
  campaignSort,
  latestAnalytics,
  latestAnalyticsForCampaign
});
