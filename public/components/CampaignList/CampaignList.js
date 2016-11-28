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
      'name': true, //false = DESC, true = ASC
      'type': false,
      'status': false,
      'actualValue': false,
      'startDate': false,
      'endDate': false
    },
    headersCssClasses: {
      'name': 'campaign-list__header-order--desc name',
      'type': 'campaign-list__header-order type',
      'status': 'campaign-list__header-order status',
      'actualValue': 'campaign-list__header-order actualValue',
      'startDate': 'campaign-list__header-order startDate',
      'endDate': 'campaign-list__header-order endDate'
    }
  };

  setCampaignSort = (c) => {
    const order = this.state.sortOrder[c] ? 'asc' : 'desc';

    this.props.uiActions.setCampaignSort(c, this.state.sortOrder[c]);
    this.state.headersCssClasses[c] = 'campaign-list__header-order--' + order + " " + c;
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
            <th onClick={ () => this.setCampaignSort('name') } className="campaign-list__header--sortable">
              <span> Name </span>
              <span className={ this.state.headersCssClasses['name'] }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('type') } className="campaign-list__header--sortable">
              <span> Type </span>
              <span className={ this.state.headersCssClasses['type'] }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('status') } className="campaign-list__header--sortable">
              <span> Status </span>
              <span className={ this.state.headersCssClasses['status'] }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('actualValue') } className="campaign-list__header--sortable">
              <span> Value </span>
              <span className={ this.state.headersCssClasses['actualValue'] }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('startDate') } className="campaign-list__header--sortable">
              <span> Start date </span>
              <span className={ this.state.headersCssClasses['startDate'] }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('endDate') } className="campaign-list__header--sortable">
              <span> Finish date </span>
              <span className={ this.state.headersCssClasses['endDate'] }> &nbsp; </span>
            </th>
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
