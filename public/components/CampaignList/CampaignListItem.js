import React, { PropTypes } from 'react';

class CampaignListItem extends React.Component {

  static propTypes = {
    campaign: PropTypes.shape({
      name: PropTypes.string,
      id: PropTypes.string
    }).isRequired
  };

  render () {
    return (
      <div className="campaign-list__item">
        <div className="campaign-list__item__name">
          {this.props.campaign.name}
        </div>
      </div>
    );
  }
}

export default CampaignListItem;
