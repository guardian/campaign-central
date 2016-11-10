import React, { PropTypes } from 'react';
import CampaignCtas from "./CampaignCtas";
import {tagEditUrl} from '../../util/urlBuilder';

class CampaignLevelAssets extends React.Component {

  renderTagInformation = () => {

    if(this.props.campaign.tagId) {
      return (
        <span className="campaign-assets__field__value">
          <a href={tagEditUrl(this.props.campaign.tagId)} target="_blank">
            <img src={this.props.campaign.campaignLogo} className="campaign-assets__field__logo"/>
            {this.props.campaign.pathPrefix}
          </a>
        </span>
      )
    }

    return (
      <span className="campaign-assets__field__value">
        No tag has been configured yet
      </span>
    )
  }

  render () {

    var ctaBlock;
    if(this.props.campaign.type === 'hosted') {
      ctaBlock = (
        <CampaignCtas campaign={this.props.campaign} />
      );
    }

    return (
      <div className="campaign-assets">
        <div className="campaign-assets__field">
          <label>Tag</label>
          {this.renderTagInformation()}
        </div>

        {ctaBlock}
      </div>
    );
  }
}

export default CampaignLevelAssets;
