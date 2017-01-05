import {fetchCampaignTargetsReport} from '../../services/CampaignsApi';

function requestCampaignTargetsReport(id) {
    return {
        type:       'CAMPAIGN_TARGETS_REPORT_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignTargetsReport(campaignTargetsReport) {
    return {
        type:        'CAMPAIGN_TARGETS_REPORT_GET_RECEIVE',
        campaignTargetsReport:    campaignTargetsReport,
        receivedAt:  Date.now()
    };
}

function errorReceivingCampaignTargetsReport(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaign targets',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignTargetsReport(id) {
    return dispatch => {
      dispatch(requestCampaignTargetsReport(id));
      return fetchCampaignTargetsReport(id)
        .catch(error => dispatch(errorReceivingCampaignTargetsReport(error)))
        .then(res => {
          dispatch(receiveCampaignTargetsReport(res));
        });
    };
}
