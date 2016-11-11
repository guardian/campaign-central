import React, { PropTypes } from 'react';
import CampaignListItem from './CampaignListItem';

class CampaignList extends React.Component {

  static propTypes = {
    campaigns: PropTypes.array
  };

  static defaultProps = {
    campaigns: []
  };

  render () {
    if (!this.props.campaigns.length) {
      return (
        <div className="campaign-list">
          No matching campaigns found
        </div>
      );
    }

    return (
      <table className="campaign-list">
        <thead>
          <tr>
            <th className="campaign-list__header">Name</th>
            <th className="campaign-list__header">Type</th>
            <th className="campaign-list__header">Status</th>
            <th className="campaign-list__header">Value</th>
            <th className="campaign-list__header">Start date</th>
            <th className="campaign-list__header">Finish date</th>
            <th className="campaign-list__header">Uniques</th>
          </tr>
        </thead>
          <tbody>
            {this.props.campaigns.map((c) => <CampaignListItem campaign={c} analyticsSummary={this.props.overallAnalyticsSummary[c.id]} key={c.id} />)}
          </tbody>
      </table>
    );
  }
}

export default CampaignList;
