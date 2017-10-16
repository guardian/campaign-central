
export default function benchmarks(state = null, action) {
  switch (action.type) {

    case 'CAMPAIGN_BENCHMARKS_GET_RECEIVE':
      return action.benchmarks || false;

    default:
      return state;
  }
}
