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
import campaignTrafficDriverStats from './campaignTrafficDriverStatsReducer';
import campaignNotes from './campaignNotesReducer';
import campaignStateFilter from './campaignStateFilterReducer';
import campaignTypeFilter from './campaignTypeFilterReducer';
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
  campaignTrafficDriverStats,
  campaignNotes,
  campaignStateFilter,
  campaignTypeFilter,
  overallAnalyticsSummary
});
