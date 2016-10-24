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
      <Link className="campaign-list__item" to={"/campaign/" + this.props.campaign.id}>
          {image}
        <div className="campaign-list__item__info">
          <span className="campaign-list__item__info-name">{this.props.campaign.name}</span>
          <span className="campaign-list__item__info-other">
            <span className="campaign-list__item__info-other--info">Status: </span>
            <span className="campaign-list__item__info-other--status">{this.props.campaign.status} </span>
          </span>
          <span className="campaign-list__item__info-other">
            <span className="campaign-list__item__info-other--info">Value: </span>
            <span className="campaign-list__item__info-other--value">{this.props.campaign.actualValue} </span>
          </span>
          <span className="campaign-list__item__info-other">
            <span className="campaign-list__item__info-other--info">Start date: </span>
            <span className="campaign-list__item__info-other--value">{startDate}</span>
          </span>
          <span className="campaign-list__item__info-other">
                <span className="campaign-list__item__info-other--info">Finish date: </span>
                <span className="campaign-list__item__info-other--value">{endDate}{daysLeft}</span>
          </span>
        </div>
      </Link>
    );
  }
}

export default CampaignListItem;
