import React, { PropTypes } from 'react';
import CampaignInformationEdit from './CampaignInformationEdit';
import CampaignTargetsEdit from './CampaignTargetsEdit';

class CampaignEdit extends React.Component {

  state = {
    isCampaignDirty: false
  }

  triggerSave = () => {
    this.props.saveCampaign(this.props.campaign.id, this.props.campaign);
    this.setState({
      isCampaignDirty: false
    });
  }

  triggerUpdate = (newCampaign) => {
    this.props.updateCampaign(newCampaign.id, newCampaign);
    this.setState({
      isCampaignDirty: true
    });
  }

  renderSaveButtons = () => {
    if (!this.state.isCampaignDirty) {
      return false;
    }

    return (
      <div className="campaign-box__footer">
        <span className="campaign-info__button" onClick={this.triggerSave}>Save</span>
      </div>
    );
  }

  render () {
    return (
      <div className="campaign-info campaign-box">
        <div className="campaign-box__header">
          Campaign Info
        </div>
        <div className="campaign-box__body">
          <div className="campaign__column">
            <CampaignInformationEdit
              campaign={this.props.campaign}
              latestAnalyticsForCampaign={this.props.latestAnalyticsForCampaign}
              updateCampaign={this.triggerUpdate} />
          </div>
          <div className="campaign__column">
            <CampaignTargetsEdit campaign={this.props.campaign} updateCampaign={this.triggerUpdate} />
          </div>
        </div>
        {this.renderSaveButtons()}
      </div>
    );
  }
}

export default CampaignEdit;
