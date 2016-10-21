import React, { PropTypes } from 'react';
import CampaignLevelAssets from './CampaignLevelAssets';
import CampaignContent from './CampaignContent';
import {refreshCampaignFromCAPI} from '../../services/CampaignsApi';

class CampaignAssets extends React.Component {

  refreshCampaign = () => {
    if (this.props.campaign.tagId) {
      refreshCampaignFromCAPI(this.props.campaign.id).then((resp) => {
        this.props.getCampaign(this.props.campaign.id);
        this.props.getCampaignContent(this.props.campaign.id);
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
          <CampaignContent campaign={this.props.campaign} getCampaignContent={this.props.getCampaignContent}/>
        </div>
      </div>
    );
  }
}

export default CampaignAssets;