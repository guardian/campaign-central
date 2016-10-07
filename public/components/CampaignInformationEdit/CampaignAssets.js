import React, { PropTypes } from 'react';
import CampaignLevelAssets from './CampaignLevelAssets';

class CampaignAssets extends React.Component {

  render () {
    return (
      <div className="campaign-info campaign-box">
        <div className="campaign-box__header">
          Assets
        </div>
        <div className="campaign-box__body">
          <CampaignLevelAssets campaign={this.props.campaign} />
        </div>
      </div>
    );
  }
}

export default CampaignAssets;