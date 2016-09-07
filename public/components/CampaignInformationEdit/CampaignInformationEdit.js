import React, { PropTypes } from 'react';
import EditableText from '../utils/EditableText';

import {formatMillisecondDate} from '../../util/dateFormatter';

class CampaignInformationEdit extends React.Component {

  static propTypes = {
    updateCampaign: PropTypes.func.isRequired,
    markDirty: PropTypes.func.isRequired
  };


  triggerUpdate = (newCampaign) => {
    this.props.markDirty();
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
            <label>Value</label>
            <EditableText value={this.props.campaign.actualValue ? "£" + this.props.campaign.actualValue : ""} onChange={this.updateCampaignValue} />
          </div>
        </div>
      </div>
    );
  }
}

export default CampaignInformationEdit;
