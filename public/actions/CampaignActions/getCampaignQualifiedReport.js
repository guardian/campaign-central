import {fetchCampaignQualifiedReport} from '../../services/CampaignsApi';

function requestCampaignQualifiedReport(id) {
    return {
        type:       'CAMPAIGN_QUALIFIED_REPORT_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignQualifiedReport(campaignQualifiedReport) {
    return {
        type:        'CAMPAIGN_QUALIFIED_REPORT_GET_RECEIVE',
        campaignQualifiedReport:    campaignQualifiedReport,
        receivedAt:  Date.now()
    };
}

function errorReceivingCampaignQualifiedReport(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaign qualified report',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignQualifiedReport(id) {
    return dispatch => {
      dispatch(requestCampaignQualifiedReport(id));
      return fetchCampaignQualifiedReport(id)
        .catch(error => dispatch(errorReceivingCampaignQualifiedReport(error)))
        .then(res => {
          dispatch(receiveCampaignQualifiedReport(res));
        });
    };
}
