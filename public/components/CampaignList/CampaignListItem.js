import React, { PropTypes } from 'react';
import {Link} from 'react-router';

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
            <span className="campaign-list__item__info-other--value">13.08.2016 </span>
          </span>
          <span className="campaign-list__item__info-other">
                <span className="campaign-list__item__info-other--info">Finish date: </span>
                <span className="campaign-list__item__info-other--value">13.11.2016 - 55 days left</span>
          </span>
        </div>
      </Link>
    );
  }
}

export default CampaignListItem;
