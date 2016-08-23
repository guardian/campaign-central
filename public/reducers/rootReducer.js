import { combineReducers } from 'redux';
import error from './errorReducer';
import config from './configReducer';
import campaigns from './campaignsReducer';
import campaign from './campaignReducer';

export default combineReducers({
  error,
  config,
  campaigns,
  campaign
});
