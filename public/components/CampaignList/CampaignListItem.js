import React, { PropTypes } from 'react';
import {Link} from 'react-router';
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../util/dateFormatter'

class CampaignListItem extends React.Component {

  static propTypes = {
    campaign: PropTypes.shape({
      name: PropTypes.string,
      id: PropTypes.string
    }).isRequired
  };

  redirectToCampaign = () => {
    window.document.location = "/campaign/" + this.props.campaign.id;
  };

  renderProgressSummary = () => {
    var targetUniques = this.props.campaign.targets ? this.props.campaign.targets.uniques : '';

    if (this.props.analyticsSummary) {
      const progressClass = (this.props.analyticsSummary.totalUniques < this.props.analyticsSummary.targetToDate) ? 'campaign-list__item--behind' : 'campaign-list__item--ahead';

      return(<td className={'campaign-list__item '+ progressClass}>
        target: {targetUniques}<br />
        progress: {this.props.analyticsSummary.totalUniques} uniques<br/>
        against: {this.props.analyticsSummary.targetToDate} expected
      </td>);
    }

    return(<td className="campaign-list__item">
      target: {targetUniques}
    </td>);
  };

  render () {

    var image;
    if (this.props.campaign.campaignLogo) {
      image = <img src={this.props.campaign.campaignLogo} className="campaign-list__item__logo"/>
    }

    var startDate = 'Not yet started';
    if (this.props.campaign.startDate) {
      startDate = shortFormatMillisecondDate(this.props.campaign.startDate);
    }

    var endDate = 'Not yet configured';
    if (this.props.campaign.endDate) {
      endDate = shortFormatMillisecondDate(this.props.campaign.endDate);
    }

    var daysLeft = '';
    if (this.props.campaign.startDate && this.props.campaign.endDate) {
      const now = new Date();
      const oneDayMillis = 24 * 60 * 60 * 1000;
      const days = Math.round((this.props.campaign.endDate - now) / oneDayMillis);

      daysLeft = ' - ' + days + ' days left';
    }

    return (
      <tr className="campaign-list__row" onClick={this.redirectToCampaign}>
        <td className="campaign-list__item">{this.props.campaign.name}<br/>{image}</td>
        <td className="campaign-list__item">{this.props.campaign.type}</td>
        <td className="campaign-list__item">{this.props.campaign.status}</td>
        <td className="campaign-list__item">{this.props.campaign.actualValue}</td>
        <td className="campaign-list__item">{startDate}</td>
        <td className="campaign-list__item">{endDate} {daysLeft}</td>
        {this.renderProgressSummary()}
      </tr>
    );
  }
}

export default CampaignListItem;
