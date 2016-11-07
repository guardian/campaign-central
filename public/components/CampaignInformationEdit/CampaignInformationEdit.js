import React, { PropTypes } from 'react';
import {campaignStatuses} from '../../constants/campaignStatuses';
import EditableText from '../utils/EditableText';
import EditableDropdown from '../utils/EditableDropdown';

import {formatMillisecondDate} from '../../util/dateFormatter';

class CampaignInformationEdit extends React.Component {

  static propTypes = {
    updateCampaign: PropTypes.func.isRequired
  };

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

    const numValue = parseInt(value) === NaN ? undefined : parseInt(value);

    this.props.updateCampaign(Object.assign({}, this.props.campaign, {
      actualValue: numValue
    }));
  }

  render () {

    var startDate = 'Not yet started';
    if (this.props.campaign.startDate) {
      startDate = formatMillisecondDate(this.props.campaign.startDate);
    }

    var endDate = 'Not yet configured';
    if (this.props.campaign.endDate) {
      endDate = formatMillisecondDate(this.props.campaign.endDate);
    }

    var daysLeft = '';
    if (this.props.campaign.startDate && this.props.campaign.endDate) {
      const now = new Date();
      const oneDayMillis = 24 * 60 * 60 * 1000;
      const days = Math.round((this.props.campaign.endDate - now) / oneDayMillis);

      daysLeft = ' - ' + days + ' days left';
    }

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
            <label>Campaign type</label>
            <span className="campaign-info__field__value">{this.props.campaign.type}</span>
          </div>
          <div className="campaign-info__field">
            <label>Value</label>
            <EditableText value={this.props.campaign.actualValue ? "£" + this.props.campaign.actualValue : ""} onChange={this.updateCampaignValue} />
          </div>
          <div className="campaign-info__field">
            <label>Status</label>
            <EditableDropdown values={campaignStatuses} name="status" selectedValue={this.props.campaign.status} onChange={this.updateCampaignStatus} />
          </div>
          <div className="campaign-info__field">
            <label>Start date</label>
            <span className="campaign-info__field__value">{startDate}</span>
          </div>
          <div className="campaign-info__field">
            <label>End date</label>
            <span className="campaign-info__field__value">{endDate}{daysLeft}</span>
          </div>
          <div className="campaign-info__field">
            <label>Created</label>
            <span className="campaign-info__field__value">{formatMillisecondDate(this.props.campaign.created)} by {this.props.campaign.createdBy.firstName} {this.props.campaign.createdBy.lastName}</span>
          </div>
          <div className="campaign-info__field">
            <label>Last modified</label>
            <span className="campaign-info__field__value">{formatMillisecondDate(this.props.campaign.lastModified)} by {this.props.campaign.lastModifiedBy.firstName} {this.props.campaign.lastModifiedBy.lastName}</span>
          </div>
        </div>
      </div>
    );
  }
}

export default CampaignInformationEdit;
