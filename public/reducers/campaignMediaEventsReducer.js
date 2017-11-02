
export default function campaignMediaEventsReducer(state = false, action) {
  switch (action.type) {

    case 'CAMPAIGN_MEDIA_EVENTS_GET_RECEIVE':
      return action.campaignMediaEvents || false;

    default:
      return state;
  }
}
