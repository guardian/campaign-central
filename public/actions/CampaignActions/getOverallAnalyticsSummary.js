import {fetchOverallAnalyticsSummary} from '../../services/CampaignsApi';

function requestOverallAnalyticsSummary(id) {
    return {
        type:       'OVERALL_ANALYTICS_SUMMARY_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveOverallAnalyticsSummary(overallAnalyticsSummary) {
    return {
        type:        'OVERALL_ANALYTICS_SUMMARY_GET_RECEIVE',
        overallAnalyticsSummary:    overallAnalyticsSummary,
        receivedAt:  Date.now()
    };
}

function errorRecievingOverallAnalyticsSummary(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get overall analytics summary',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getOverallAnalyticsSummary() {
    return dispatch => {
      dispatch(requestOverallAnalyticsSummary());
      return fetchOverallAnalyticsSummary()
        .catch(error => dispatch(errorRecievingOverallAnalyticsSummary(error)))
        .then(res => {
          dispatch(receiveOverallAnalyticsSummary(res));
        });
    };
}
