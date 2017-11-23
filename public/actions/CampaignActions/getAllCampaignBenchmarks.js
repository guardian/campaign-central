import {getCampaignBenchmarks} from '../../services/CampaignsApi';

function requestBenchmarks() {
    return {
        type:       'CAMPAIGN_BENCHMARKS_GET_REQUEST',
        receivedAt: Date.now()
    };
}

function receiveBenchmarks(benchmarks) {
    return {
        type:        'CAMPAIGN_BENCHMARKS_GET_RECEIVE',
        benchmarks:    benchmarks,
        receivedAt:  Date.now()
    };
}

function errorRecievingBenchmarks(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get bencharks',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getBenchmarks(territory) {
    return dispatch => {
      dispatch(requestBenchmarks());
      return getCampaignBenchmarks(territory)
        .catch(error => dispatch(errorRecievingBenchmarks(error)))
        .then(res => {
          dispatch(receiveBenchmarks(res));
        });
    };
}
