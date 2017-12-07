import React, { PropTypes } from 'react';
import R from 'ramda';
import AddTargetControl from './AddTargetControl';
import EditableNumber from '../utils/EditableNumber';
import { defaultTargets } from '../../constants/defaultTargets'

class CampaignTargetsEdit extends React.Component {

  static propTypes = {
    updateCampaign: PropTypes.func.isRequired
  };

  updateTargetValue = (target, number) => {
    const updatedTargets = Object.assign({}, this.props.campaign.targets, { [target]: number })

    this.props.updateCampaign(Object.assign({}, this.props.campaign, {
      targets: updatedTargets
    }));
  }

  deleteTarget = (target) => {
    const updatedTargets = R.omit(target, this.props.campaign.targets);

    this.props.updateCampaign(Object.assign({}, this.props.campaign, {
      targets: updatedTargets
    }));
  }

  formatTargetName = k => {
    const targetInfo = defaultTargets.find(t => t.value === k);
    return targetInfo ? targetInfo.name : k;
  }

  renderTargetsList = () => {

    const keys = Object.keys(this.props.campaign.targets).sort();
    
    return (
      <ul>
        {keys.map((k) =>
          <div className="campaign-info__field" key={k} >
            <label>{ this.formatTargetName(k) }</label>
            <EditableNumber value={this.props.campaign.targets[k]} onNumberChange={ (n) => this.updateTargetValue(k, n)} />
            <div className="editable-text__button" onClick={ () => this.deleteTarget(k) } >
              <i className="i-delete"/>
            </div>
          </div>
        )}
      </ul>
    );
  }

  render () {
    return (
      <div className="campaign-info campaign-box-section">
        <div className="campaign-box-section__header">
          Campaign Targets
        </div>
        <div className="campaign-box-section__body">
          <div>
            {this.renderTargetsList()}
          </div>
          <AddTargetControl existingTargets={Object.keys(this.props.campaign.targets)} onTargetAdded={this.updateTargetValue} />
        </div>
      </div>
    );
  }
}

export default CampaignTargetsEdit;
