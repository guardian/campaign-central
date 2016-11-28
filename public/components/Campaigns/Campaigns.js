import React, {Component, PropTypes} from 'react';
import CampaignList from '../CampaignList/CampaignList';

class Campaigns extends Component {

  static propTypes = {
    campaigns: PropTypes.array.isRequired,
  }

  filterCampaigns = (campaigns) => {
    var filtered = campaigns;

    if (this.props.campaignStateFilter) {
      filtered = filtered.filter((c) => c.status === this.props.campaignStateFilter);
    }

    if (this.props.campaignTypeFilter) {
      filtered = filtered.filter((c) => c.type === this.props.campaignTypeFilter);
    }
    return filtered;
  }

  sortBy = (field, reverse, iteratees) => {
    let key = function(x) {return iteratees ? iteratees(x[field]) : x[field]};

    reverse = !reverse ? 1 : -1;

    return function (a, b) {
      return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
    }
  }

  sortCampaigns = (campaigns) => {
    let sorted = campaigns;
    let column = this.props.campaignSortColumn || 'name';
    let order = this.props.campaignSortOrder;

    //console.log('inside sortCampaigns: ', sorted, column, order, this.props.campaignSortColumn);

    sorted = sorted.sort(this.sortBy(column, order, function(value) {
      if (typeof value === "string" && column === ('name' || 'type' || 'status')) {
        return value.toUpperCase();
      } else if (typeof value === "string" && column === ('actualValue' || 'startDate' || 'endDate')) {
        return parseInt(value, 10);
      } else {
        return value;
      }
    }));

    return sorted;
  }

  componentDidMount() {
    this.props.campaignActions.getCampaigns();
    this.props.analyticsActions.getOverallAnalyticsSummary();
  }

  render() {

    return (
      <div className="campaigns">
        <h2 className="campaigns__header">Campaigns</h2>
        <CampaignList campaigns={this.filterCampaigns(this.sortCampaigns(this.props.campaigns))} overallAnalyticsSummary={this.props.overallAnalyticsSummary} />
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaigns from '../../actions/CampaignActions/getCampaigns';
import * as getOverallAnalyticsSummary from '../../actions/CampaignActions/getOverallAnalyticsSummary';

function mapStateToProps(state) {
  console.log(state);
  return {
    campaigns: state.campaigns,
    overallAnalyticsSummary: state.overallAnalyticsSummary,
    campaignStateFilter: state.campaignStateFilter,
    campaignTypeFilter: state.campaignTypeFilter,
    campaignSortColumn: state.campaignSort.campaignSortColumn,
    campaignSortOrder: state.campaignSort.campaignSortOrder
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignActions: bindActionCreators(Object.assign({}, getCampaigns), dispatch),
    analyticsActions: bindActionCreators(Object.assign({}, getOverallAnalyticsSummary), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Campaigns);
