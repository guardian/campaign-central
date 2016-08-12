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
    if (!this.props.campaigns.length) {
      return (
        <div className="campaign-list">
          No matching campaigns found
        </div>
      );
    }

    return (
      <div className="campaign-list">
        {this.props.campaigns.map((c) => <CampaignListItem campaign={c} key={c.id} />)}
      </div>
    );
  }
}

export default CampaignList;
