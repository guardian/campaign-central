import React, { PropTypes } from 'react';
import CampaignListItem from './CampaignListItem';

class CampaignList extends React.Component {

  static propTypes = {
    campaigns: PropTypes.array
  };

  static defaultProps = {
    campaigns: []
  };

  sortOrder = {
    'name': true, //false = DESC, true = ASC
    'type': false,
    'status': false,
    'actualValue': false,
    'startDate': false,
    'endDate': false
  };

  setHeaderCssClass = (currentHeader) => {
    let sortedColumn = this.props.campaignSortColumn || 'name';
    let order = this.sortOrder[sortedColumn] ? 'asc' : 'desc';
    let cssClass = 'campaign-list__header-order';

    if (currentHeader === sortedColumn) {
      cssClass = 'campaign-list__header-order--' + order;
    }

    return cssClass;
  };

  setCampaignSort = (c) => {
    //sort campaign list
    this.props.uiActions.setCampaignSort(c, this.sortOrder[c]);

    //set a new sort order
    this.sortOrder[c] = !this.sortOrder[c];
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
            <th onClick={ () => this.setCampaignSort('name') } className="campaign-list__header--sortable name">
              <span> Name </span>
              <span className={ this.setHeaderCssClass('name') }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('type') } className="campaign-list__header--sortable type">
              <span> Type </span>
              <span className={ this.setHeaderCssClass('type') }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('status') } className="campaign-list__header--sortable status">
              <span> Status </span>
              <span className={ this.setHeaderCssClass('status') }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('actualValue') } className="campaign-list__header--sortable actualValue">
              <span> Value </span>
              <span className={ this.setHeaderCssClass('actualValue') }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('startDate') } className="campaign-list__header--sortable startDate">
              <span> Start date </span>
              <span className={ this.setHeaderCssClass('startDate') }> &nbsp; </span>
            </th>
            <th onClick={ () => this.setCampaignSort('endDate') } className="campaign-list__header--sortable endDate">
              <span> Finish date </span>
              <span className={ this.setHeaderCssClass('endDate') }> &nbsp; </span>
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
