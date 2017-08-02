import React, { PropTypes } from 'react';
import {Link} from 'react-router';
import {shortFormatMillisecondDate} from '../../util/dateFormatter';

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

  daysBetween = (date1, date2) => {
    const oneDayMillis = 24 * 60 * 60 * 1000;
    return Math.ceil((date2 - date1) / oneDayMillis);
  };
  renderProgressSummary = () => {
      return <td className="campaign-list__item">-</td>;
  };

  render () {

    var image = this.props.campaign.campaignLogo && <img src={this.props.campaign.campaignLogo} className="campaign-list__item__logo"/>;
    var startDate = this.props.campaign.startDate ? shortFormatMillisecondDate(this.props.campaign.startDate) : 'Not yet started';
    var endDate = this.props.campaign.endDate ? shortFormatMillisecondDate(this.props.campaign.endDate) : 'Not yet configured';

    var daysLeft = '';
    if (this.props.campaign.startDate && this.props.campaign.endDate) {
      const days = this.daysBetween(new Date(), this.props.campaign.endDate);

      var dayWord = Math.abs(days) === 1 ? ' day' : ' days';

      daysLeft = days < 0 ? 'Ended ' + (-days) + dayWord + ' ago' : days + dayWord + ' left';
    }

    return (
      <tr className="campaign-list__row" onClick={this.redirectToCampaign}>
        <td className="campaign-list__item">{this.props.campaign.name}{image}</td>
        <td className="campaign-list__item">{this.props.campaign.type}</td>
        <td className="campaign-list__item">{this.props.campaign.status}</td>
        <td className="campaign-list__item">{this.props.campaign.actualValue}</td>
        <td className="campaign-list__item">{startDate}</td>
        <td className="campaign-list__item">{endDate}</td>
        <td className="campaign-list__item">{daysLeft}</td>
        <td className="campaign-list__item">{this.props.campaign.targets && this.props.campaign.targets.uniques}</td>
        {this.renderProgressSummary()}
      </tr>
    );
  }
}

export default CampaignListItem;
