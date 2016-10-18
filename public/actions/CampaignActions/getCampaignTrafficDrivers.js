import {fetchCampaignTrafficDrivers} from '../../services/CampaignsApi';

function requestCampaignTrafficDrivers(id) {
   return {
        type:       'TRAFFIC_DRIVERS_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignTrafficDrivers(trafficDrivers) {
    return {
        type:                   'TRAFFIC_DRIVERS_GET_RECEIVE',
        campaignTrafficDrivers: trafficDrivers,
        receivedAt:             Date.now()
    };
}

function errorReceivingCampaignTrafficDrivers(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get traffic drivers',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignTrafficDrivers(id) {
    return dispatch => {
      dispatch(requestCampaignTrafficDrivers(id));
      return fetchCampaignTrafficDrivers(id)
        .catch(error => dispatch(errorReceivingCampaignTrafficDrivers(error)))
        .then(res => {

//          alert('here');
//          alert(res[0]);
//          for(var propertyName in res[0]) {
//            alert(res[0][propertyName]);
//          }

          dispatch(receiveCampaignTrafficDrivers(res));
        });
    };
}
