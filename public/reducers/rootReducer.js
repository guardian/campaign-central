import {CLEAR_ERROR} from '../actions/UIActions/clearError';
import {SHOW_ERROR} from '../actions/UIActions/showError';


export default function tag(state = {
  error: false,
  config: {},
}, action) {
  switch (action.type) {

// CONFIG

  case 'CONFIG_RECEIVED':
    return Object.assign({}, state, {
      config: action.config
    });

// UI

  case CLEAR_ERROR:
    return Object.assign({}, state, {
      error: false
    });

  case SHOW_ERROR:
    return Object.assign({}, state, {
      error: action.message
    });

  default:
    return state;
  }
}
