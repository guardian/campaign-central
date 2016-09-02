import React, { PropTypes } from 'react';
import R from 'ramda';
import EditableNumber from '../utils/EditableNumber';

class CampaignTargetsEdit extends React.Component {

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
    this.setState({
      isCampaignDirty: true
    });

    this.props.updateCampaign(newCampaign.id, newCampaign);
  }

  updateTargetValue = (target, number) => {
    const updatedTargets = Object.assign({}, this.props.campaign.targets, { [target]: number })

    this.triggerUpdate(Object.assign({}, this.props.campaign, {
      targets: updatedTargets
    }));
  }

  deleteTarget = (target) => {
    const updatedTargets = R.omit(target, this.props.campaign.targets);

    this.triggerUpdate(Object.assign({}, this.props.campaign, {
      targets: updatedTargets
    }));
  }

  renderTargetsList = () => {

    const keys = Object.keys(this.props.campaign.targets).sort();

    return (
      <ul>
        {keys.map((k) =>
          <div className="campaign-info__field" key={k} >
            <label>{k}</label>
            <EditableNumber value={this.props.campaign.targets[k]} onNumberChange={this.updateTargetValue.bind(this, k)} />
            <div className="editable-text__button" onClick={this.deleteTarget.bind(this, k)} >
              <i className="i-delete"/>
            </div>
          </div>
        )}
      </ul>
    );
  }
  
  render () {
    return (
      <div className="campaign-info campaign-box">
        <div className="campaign-box__header">
          Campaign Targets
        </div>
        <div className="campaign-box__body">
          <div>
            {this.renderTargetsList()}
          </div>
        </div>
      </div>
    );
  }
}

export default CampaignTargetsEdit;
