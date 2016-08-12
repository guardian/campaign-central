import { combineReducers } from 'redux';
import error from './errorReducer';
import config from './configReducer';
import campaigns from './campaignsReducer';

export default combineReducers({
  error,
  config,
  campaigns
});
