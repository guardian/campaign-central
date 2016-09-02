import React, { PropTypes } from 'react';
import {Link} from 'react-router';

class CampaignListItem extends React.Component {

  static propTypes = {
    campaign: PropTypes.shape({
      name: PropTypes.string,
      id: PropTypes.string,
      client: PropTypes.shape({name: PropTypes.string})
    }).isRequired
  };

  render () {
    return (
      <Link className="campaign-list__item" to={"/campaign/" + this.props.campaign.id}>
        <div className="campaign-list__item__name">
          {this.props.campaign.client.name}: {this.props.campaign.name}
        </div>
      </Link>
    );
  }
}

export default CampaignListItem;
