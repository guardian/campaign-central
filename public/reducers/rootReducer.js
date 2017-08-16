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
import clients from './clientsReducer';
import client from './clientReducer';
import campaignContent from './campaignContentReducer';
import campaignTrafficDrivers from './campaignTrafficDriverReducer';
import campaignTrafficDriversDirty from './campaignTrafficDriversDirtyReducer';
import campaignTrafficDriverSuggestions from './campaignTrafficDriverSuggestionsReducer';
import campaignTrafficDriverSuggestionsDirty from './campaignTrafficDriverSuggestionsDirtyReducer';
import campaignTrafficDriverStats from './campaignTrafficDriverStatsReducer';
import campaignNotes from './campaignNotesReducer';
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
  clients,
  client,
  campaignContent,
  campaignTrafficDrivers,
  campaignTrafficDriversDirty,
  campaignTrafficDriverSuggestions,
  campaignTrafficDriverSuggestionsDirty,
  campaignTrafficDriverStats,
  campaignNotes,
  campaignSort,
  latestAnalytics,
  latestAnalyticsForCampaign
});
