import React, { PropTypes } from 'react';
import CampaignListItem from './CampaignListItem';

class CampaignList extends React.Component {

  static propTypes = {
    campaigns: PropTypes.array,
    sortCampaigns: PropTypes.func.isRequired
  };

  static defaultProps = {
    campaigns: []
  };

  invokeSort = (column) => {
    let iterateesFunc = (value) => {
        if (typeof value === "string" && column === ('name' || 'type' || 'status')) {
          return value.toUpperCase();
        } else if (typeof value === "string" && column === ('actualValue' || 'startDate' || 'endDate')) {
          return parseInt(value, 10);
        } else {
          return value;
        }
    }

    this.props.sortCampaigns(column, false, iterateesFunc);
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
      <table className="campaign-list">
        <thead>
          <tr>
            <th onClick={ () => this.invokeSort('name') } className="campaign-list__header">Name</th>
            <th onClick={ () => this.invokeSort('type') } className="campaign-list__header">Type</th>
            <th onClick={ () => this.invokeSort('status') } className="campaign-list__header">Status</th>
            <th onClick={ () => this.invokeSort('actualValue') } className="campaign-list__header">Value</th>
            <th onClick={ () => this.invokeSort('startDate') } className="campaign-list__header">Start date</th>
            <th onClick={ () => this.invokeSort('endDate') } className="campaign-list__header">Finish date</th>
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
