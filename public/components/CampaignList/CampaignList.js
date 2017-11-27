import React, { PropTypes } from 'react';
import CampaignListItem from './CampaignListItem';

class CampaignList extends React.Component {

  static propTypes = {
    campaigns: PropTypes.array
  };

  static defaultProps = {
    campaignSortColumn: 'endDate',
    campaignSortOrder: 1, //asc
    campaigns: []
  };

  sortOrderClass = (column) => {
    let order = this.props.campaignSortOrder === 1 ? 'asc' : 'desc';

    return column === this.props.campaignSortColumn ? 'campaign-list__header-order--' + order : '';
  };

  setCampaignSort = (column) => {
    var currentColumn = this.props.campaignSortColumn;
    var currentOrder = this.props.campaignSortOrder;
    this.props.uiActions.setCampaignSort(column, column !== currentColumn ? 1 : -currentOrder);
  };

  sortableColumnHead = (columnName, displayName) => {
    return (
      <th onClick={ () => this.setCampaignSort(columnName) } className="campaign-list__header campaign-list__header--sortable">
        <span className="campaign-list__header-title">{displayName}</span>
        <i className={'campaign-list__header-order ' + this.sortOrderClass(columnName) }/>
      </th>
    )
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
            {this.sortableColumnHead('name', 'Name')}
            {this.sortableColumnHead('type', 'Type')}
            {this.sortableColumnHead('status', 'Status')}
            {this.sortableColumnHead('startDate', 'Start date')}
            {this.sortableColumnHead('endDate', 'Finish date')}
            <th className="campaign-list__header">Days left</th>
            <th className="campaign-list__header">Target</th>
            <th className="campaign-list__header">Uniques</th>
            <th className="campaign-list__header">Pageviews</th>
            <th className="campaign-list__header">Production Office</th>
          </tr>
        </thead>
          <tbody>
            {this.props.campaigns.map((c) => <CampaignListItem campaign={c} latestAnalytics={this.props.latestAnalytics[c.id]} key={c.id} />)}
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
