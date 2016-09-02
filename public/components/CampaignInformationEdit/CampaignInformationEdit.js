import React, { PropTypes } from 'react';
import EditableText from '../utils/EditableText';
import EditableDropdown from '../utils/EditableDropdown';

import {formatMillisecondDate} from '../../util/dateFormatter';

class CampaignInformationEdit extends React.Component {

  stateValues = { //Not sure if this is the best place for it. Maybe we should have dedicated module with those values and import it here, in sidebar and filters?
    live: 'Live',
    prospect: 'Prospect',
    production: 'Production',
    dead: 'Dead'
  }

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

  updateCampaignName = (e) => {
    this.triggerUpdate(Object.assign({}, this.props.campaign, {
      name: e.target.value
    }));
  }

  updateCampaignValue = (e) => {
    const fieldValue = e.target.value;
    const value = fieldValue[0] === '£' ? fieldValue.substr(1, fieldValue.length) : fieldValue; //Strip out £

    const numValue = parseInt(value) === NaN ? undefined : parseInt(value);

    this.triggerUpdate(Object.assign({}, this.props.campaign, {
      actualValue: numValue
    }));
  }

  updateCampaignStatus = (e) => {
    this.triggerUpdate(Object.assign({}, this.props.campaign, {
      status: e.target.value
    }));
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
          <div className="campaign-info__field">
            <label>Name</label>
            <EditableText value={this.props.campaign.name} onChange={this.updateCampaignName} />
          </div>
          <div className="campaign-info__field">
            <label>Created</label>
            <span className="campaign-info__field__value">{formatMillisecondDate(this.props.campaign.created)} by {this.props.campaign.createdBy.firstName} {this.props.campaign.createdBy.lastName}</span>
          </div>
          <div className="campaign-info__field">
            <label>Last modified</label>
            <span className="campaign-info__field__value">{formatMillisecondDate(this.props.campaign.lastModified)} by {this.props.campaign.lastModifiedBy.firstName} {this.props.campaign.lastModifiedBy.lastName}</span>
          </div>
          <div className="campaign-info__field">
            <label>Value</label>
            <EditableText value={this.props.campaign.actualValue ? "£" + this.props.campaign.actualValue : ""} onChange={this.updateCampaignValue} />
          </div>
          <div className="campaign-info__field">
            <label>Status</label>
            <EditableDropdown values={this.stateValues} name="status" selectedValue={this.props.campaign.status} onChange={this.updateCampaignStatus} />
          </div>
        </div>
        {this.renderSaveButtons()}
      </div>
    );
  }
}

export default CampaignInformationEdit;
