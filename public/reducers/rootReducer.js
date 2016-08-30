import { combineReducers } from 'redux';
import error from './errorReducer';
import config from './configReducer';
import campaigns from './campaignsReducer';
import campaign from './campaignReducer';
import campaignAnalytics from './campaignAnalyticsReducer';

export default combineReducers({
  error,
  config,
  campaigns,
  campaign,
  campaignAnalytics
});
