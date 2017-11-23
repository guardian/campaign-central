import {fetchLatestAnalytics} from '../../services/CampaignsApi';

function requestLatestAnalytics() {
    return {
        type:       'LATEST_ANALYTICS_GET_REQUEST',
        receivedAt: Date.now()
    };
}

function receiveLatestAnalytics(latestAnalytics) {
    return {
        type:        'LATEST_ANALYTICS_GET_RECEIVE',
      latestAnalytics:    latestAnalytics,
        receivedAt:  Date.now()
    };
}

function errorRecievingLatestAnalytics(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get latest analytics',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getLatestAnalytics(territory) {
    return dispatch => {
      dispatch(requestLatestAnalytics());
      return fetchLatestAnalytics(territory)
        .catch(error => dispatch(errorRecievingLatestAnalytics(error)))
        .then(res => {
          dispatch(receiveLatestAnalytics(res));
        });
    };
}
