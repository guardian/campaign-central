import React, { PropTypes } from 'react';
import EditableText from '../utils/EditableText';

import {formatMillisecondDate} from '../../util/dateFormatter';

class CampaignInformationEdit extends React.Component {

  state = {
    isCampaignDirty: false,
    error: '',
    editable: false
  }

  enableEditing = () => {
    this.setState({
        editable: true,
        error: ''
    });
  }

  disableEditing = () => {
    this.setState({
        editable: false,
        error: ''
    });
  }

  triggerSave = () => {
    this.props.saveCampaign(this.props.campaign.id, this.props.campaign);
    this.setState({
      isCampaignDirty: false,
      error: ''
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
    const numValue = parseInt(value);

    if (isNaN(numValue) || numValue != value) {
        this.setState({
            error: 'Campaign value has to be a number!'
        });
    } else {
        this.setState({
            error: ''
        });
    }

    this.triggerUpdate(Object.assign({}, this.props.campaign, {
      actualValue: numValue
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
            <label>Value</label>
            <EditableText value={this.props.campaign.actualValue ? "£" + this.props.campaign.actualValue : ""} onChange={this.updateCampaignValue} disableEditing={this.disableEditing} enableEditing={this.enableEditing} editable={this.state.editable}/>
          </div>
          <div className="campaign-info__field">
            <span className="campaign-info__field__error">{this.state.error}</span>
          </div>
        </div>
        {this.renderSaveButtons()}
      </div>
    );
  }
}

export default CampaignInformationEdit;
