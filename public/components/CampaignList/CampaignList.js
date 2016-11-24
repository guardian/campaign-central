import React, { PropTypes } from 'react';
import CampaignListItem from './CampaignListItem';

class CampaignList extends React.Component {

  static propTypes = {
    campaigns: PropTypes.array
  };

  static defaultProps = {
    campaigns: []
  };

  sortBy = (field, reverse, iteratees) => {
    let key = function(x) {return iteratees ? iteratees(x[field]) : x[field]};

    reverse = !reverse ? 1 : -1;

    return function (a, b) {
      return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
    }
  }

  invokeSort = (column) => {
    console.log(this.props.campaigns);
    this.props.campaigns.sort(this.sortBy(column, false, function(value){
        return typeof value === "string" ? value.toUpperCase() : value
      }
    ));
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
            <th onClick={this.invokeSort('name')} className="campaign-list__header">Name</th>
            <th onClick={this.invokeSort('type')} className="campaign-list__header">Type</th>
            <th onClick={this.invokeSort('status')} className="campaign-list__header">Status</th>
            <th onClick={this.invokeSort('actualValue')} className="campaign-list__header">Value</th>
            <th onClick={this.invokeSort('startDate')} className="campaign-list__header">Start date</th>
            <th onClick={this.invokeSort('endDate')} className="campaign-list__header">Finish date</th>
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
