import React, { PropTypes } from 'react';
import {campaignStatuses} from '../../constants/campaignStatuses';
import EditableText from '../utils/EditableText';
import EditableDropdown from '../utils/EditableDropdown';

import {formatMillisecondDate} from '../../util/dateFormatter';

class CampaignInformationEdit extends React.Component {

  static propTypes = {
    updateCampaign: PropTypes.func.isRequired
  }

  state = {
    error: ''
  }

  updateCampaignName = (e) => {
    this.props.updateCampaign(Object.assign({}, this.props.campaign, {
      name: e.target.value
    }));
  }

  updateCampaignStatus = (e) => {
    this.props.updateCampaign(Object.assign({}, this.props.campaign, {
      status: e.target.value
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

    this.props.updateCampaign(Object.assign({}, this.props.campaign, {
      actualValue: numValue
    }));
  }

  render () {

    return (
      <div className="campaign-info campaign-box-section">
        <div className="campaign-box-section__header">
          Campaign Info
        </div>
        <div className="campaign-box-section__body">
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
            <EditableText value={this.props.campaign.actualValue ? "£" + this.props.campaign.actualValue : ""} onChange={this.updateCampaignValue} error={this.state.error}/>
          </div>
          <div className="campaign-info__field">
            <label>Status</label>
            <EditableDropdown values={campaignStatuses} name="status" selectedValue={this.props.campaign.status} onChange={this.updateCampaignStatus} />
          </div>
        </div>
      </div>
    );
  }
}

export default CampaignInformationEdit;
