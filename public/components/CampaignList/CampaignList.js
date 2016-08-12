import React, { PropTypes } from 'react';
import CampaignListItem from './CampaignListItem';

class CampaignList extends React.Component {

  static propTypes = {
    campaigns: PropTypes.array
  };

  static defaultProps = {
    campaigns: []
  }

  render () {
    if (!this.props.campaigns && !this.props.campaigns.length) {
      return false;
    }

    return (
      <div className="campaign-list">
        {this.props.campaigns.map((c) => <CampaignListItem campaign={c} />)}
      </div>
    );
  }
}

export default CampaignList;
