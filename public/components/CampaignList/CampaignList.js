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

  setCampaignSort = (c) => {
    this.props.uiActions.setCampaignSort(c);
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
            <th onClick={ () => this.setCampaignSort('name') } className="campaign-list__header">Name</th>
            <th onClick={ () => this.setCampaignSort('type') } className="campaign-list__header">Type</th>
            <th onClick={ () => this.setCampaignSort('status') } className="campaign-list__header">Status</th>
            <th onClick={ () => this.setCampaignSort('actualValue') } className="campaign-list__header">Value</th>
            <th onClick={ () => this.setCampaignSort('startDate') } className="campaign-list__header">Start date</th>
            <th onClick={ () => this.setCampaignSort('endDate') } className="campaign-list__header">Finish date</th>
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
    setCampaignSort: state.setCampaignSort
  };
}

function mapDispatchToProps(dispatch) {
  return {
    uiActions: bindActionCreators(Object.assign({}, setCampaignSort), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignList);
