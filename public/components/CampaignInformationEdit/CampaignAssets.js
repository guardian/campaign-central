import React, { PropTypes } from 'react';
import CampaignLevelAssets from './CampaignLevelAssets';
import CampaignContent from './CampaignContent';
import {refreshCampaignFromCAPI} from '../../services/CampaignsApi';

class CampaignAssets extends React.Component {

  refreshCampaign = () => {
    if (this.props.campaign.tagId) {
      refreshCampaignFromCAPI(this.props.campaign.id).then((resp) => {
        this.props.campaignActions.getCampaign(this.props.campaign.id);
        this.props.campaignActions.getCampaignContent(this.props.campaign.id);
      });
    }
  };

  render () {
    return (
      <div className="campaign-info campaign-box">
        <div className="campaign-box__header">
          Assets
          <span><i className="i-refresh campaign-box__header__refresh-asset-button" onClick={this.refreshCampaign} /></span>
        </div>
        <div className="campaign-box__body">
          <CampaignLevelAssets campaign={this.props.campaign} />
          <CampaignContent campaign={this.props.campaign} />
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaign from '../../actions/CampaignActions/getCampaign';
import * as getCampaignContent from '../../actions/CampaignActions/getCampaignContent';

function mapStateToProps() {
  return {};
}

function mapDispatchToProps(dispatch) {
  return {
    campaignActions: bindActionCreators(Object.assign({}, getCampaign, getCampaignContent), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignAssets);