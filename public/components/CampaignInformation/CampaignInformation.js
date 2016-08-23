import React, { PropTypes } from 'react'
import CampaignAnalytics from './CampaignAnalytics';

class CampaignInformation extends React.Component {
  render () {
    return (
      <div className="campaign-info">
        <div className="campaign-info__header">
          Campaign Info
        </div>
        <div className="campaign-info__body">
          Here's where all the text goes
        </div>

        <div className="campaign-info__header">
          Analytics
        </div>
        <CampaignAnalytics campaign={this.props.campaign} />
      </div>
    );
  }
}

export default CampaignInformation;
