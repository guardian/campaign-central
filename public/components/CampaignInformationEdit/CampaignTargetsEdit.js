import React, { PropTypes } from 'react';
import R from 'ramda';
import AddTargetControl from './AddTargetControl';
import EditableNumber from '../utils/EditableNumber';
import { defaultTargets } from '../../constants/defaultTargets'

class CampaignTargetsEdit extends React.Component {

  static propTypes = {
    updateCampaign: PropTypes.func.isRequired
  };

  updateTargetValue = (metricTarget, territory, number) => {
    const updated = Object.assign({}, this.props.campaign.campaignTargets[metricTarget], { [territory]: number })
    const updatedTargets = Object.assign({}, this.props.campaign.campaignTargets, { [metricTarget]: updated })

    this.props.updateCampaign(Object.assign({}, this.props.campaign, {
      campaignTargets: updatedTargets
    }));
  }

  deleteTarget = (metricTarget, target) => {
    const updatedTargets = R.omit(target, this.props.campaign.campaignTargets[metricTarget]);

    this.props.updateCampaign(Object.assign({}, this.props.campaign, {
      campaignTargets: updatedTargets
    }));
  }

  formatTargetName = k => {
    const targetInfo = defaultTargets.find(t => t.value === k);
    return targetInfo ? targetInfo.name : k;
  }

  renderTargetsList = () => {

    const keys = Object.keys(this.props.campaign.campaignTargets).sort();
    Object.entries(this.props.analyticsBreakdown || {});

    return (
      <ul>
        {keys.map((k) =>
          <div className="campaign-info__field" key={k} >
            <label>{ this.formatTargetName(k) }</label>
              {Object.entries(this.props.campaign.campaignTargets[k]).map( ([territory, value]) =>
                <div key={territory}>
                  <label style={{textIndent: '10px'}}>{territory}</label>
                  <EditableNumber value={value} onNumberChange={ (n) => this.updateTargetValue(k, territory, n)} />
                  <div className="editable-text__button" onClick={ () => this.deleteTarget(k, territory) } >
                    <i className="i-delete"/>
                  </div>
                </div>
              )}
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
          <AddTargetControl existingTargets={this.props.campaign.campaignTargets} onTargetAdded={this.updateTargetValue} />
        </div>
      </div>
    );
  }
}

export default CampaignTargetsEdit;
