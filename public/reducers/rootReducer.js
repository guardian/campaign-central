import { combineReducers } from 'redux';
import error from './errorReducer';
import config from './configReducer';
import campaigns from './campaignsReducer';
import campaign from './campaignReducer';
import campaignAnalytics from './campaignAnalyticsReducer';
import campaignCtaStats from './campaignCtaStatsReducer';
import clients from './clientsReducer';
import client from './clientReducer';
import campaignContent from './campaignContentReducer';
import campaignTrafficDrivers from './campaignTrafficDriverReducer';
import campaignTrafficDriverSuggestions from './campaignTrafficDriverSuggestionsReducer';
import campaignTrafficDriversDirty from './campaignTrafficDriversDirtyReducer';
import campaignTrafficDriverStats from './campaignTrafficDriverStatsReducer';
import campaignNotes from './campaignNotesReducer';
import campaignSort from './campaignSortReducer';
import overallAnalyticsSummary from './overallAnalyticsSummaryReducer';

export default combineReducers({
  error,
  config,
  campaigns,
  campaign,
  campaignAnalytics,
  campaignCtaStats,
  clients,
  client,
  campaignContent,
  campaignTrafficDrivers,
  campaignTrafficDriverSuggestions,
  campaignTrafficDriversDirty,
  campaignTrafficDriverStats,
  campaignNotes,
  campaignSort,
  overallAnalyticsSummary
});
