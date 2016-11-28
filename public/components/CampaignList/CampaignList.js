import React, { PropTypes } from 'react';
import CampaignListItem from './CampaignListItem';

class CampaignList extends React.Component {

  static propTypes = {
    campaigns: PropTypes.array
  };

  static defaultProps = {
    campaigns: []
  };

  state = {
    sortOrder: {
      'name': false, //false = DESC, true = ASC
      'type': false,
      'status': false,
      'actualValue': false,
      'startDate': false,
      'endDate': false
    },
    headersCssClasses: {
      'name': 'campaign-list__header--sortable', //default class name for the sortable headers
      'type': 'campaign-list__header--sortable',
      'status': 'campaign-list__header--sortable',
      'actualValue': 'campaign-list__header--sortable',
      'startDate': 'campaign-list__header--sortable',
      'endDate': 'campaign-list__header--sortable'
    }
  };

  setCampaignSort = (c) => {
    this.props.uiActions.setCampaignSort(c, this.state.sortOrder[c]);
    const order = this.state.sortOrder[c] ? 'asc' : 'desc';
    console.log(this.state.headersCssClasses[c], order);
    this.state.headersCssClasses[c] = 'campaign-list__header--sorted-' + order;
    this.state.sortOrder[c] = !this.state.sortOrder[c];
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
            <th onClick={ () => this.setCampaignSort('name') } className={ this.state.headersCssClasses['name'] }>Name</th>
            <th onClick={ () => this.setCampaignSort('type') } className={ this.state.headersCssClasses['type'] }>Type</th>
            <th onClick={ () => this.setCampaignSort('status') } className={ this.state.headersCssClasses['status'] }>Status</th>
            <th onClick={ () => this.setCampaignSort('actualValue') } className={ this.state.headersCssClasses['actualValue'] }>Value</th>
            <th onClick={ () => this.setCampaignSort('startDate') } className={ this.state.headersCssClasses['startDate'] }>Start date</th>
            <th onClick={ () => this.setCampaignSort('endDate') } className={ this.state.headersCssClasses['endDate'] }>Finish date</th>
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

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as setCampaignSort from '../../actions/UIActions/setCampaignSort';

function mapStateToProps(state) {
  return {
    campaignSortColumn: state.campaignSort.campaignSortColumn,
    campaignSortOrder: state.campaignSort.campaignSortOrder
  };
}

function mapDispatchToProps(dispatch) {
  return {
    uiActions: bindActionCreators(Object.assign({}, setCampaignSort), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignList);
