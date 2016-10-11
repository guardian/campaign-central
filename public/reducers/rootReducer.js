import { combineReducers } from 'redux';
import error from './errorReducer';
import config from './configReducer';
import campaigns from './campaignsReducer';
import campaign from './campaignReducer';
import campaignAnalytics from './campaignAnalyticsReducer';
import clients from './clientsReducer';
import client from './clientReducer';
import campaignContent from './campaignContentReducer';
import campaignNotes from './campaignNotesReducer';

export default combineReducers({
  error,
  config,
  campaigns,
  campaign,
  campaignAnalytics,
  clients,
  client,
  campaignContent,
  campaignNotes
});
