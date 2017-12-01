import React, {Component, PropTypes} from 'react';
import CampaignList from '../CampaignList/CampaignList';

class Campaigns extends Component {

  static propTypes = {
    campaigns: PropTypes.array.isRequired,
  }

  static defaultProps = {
    campaignSortColumn: 'endDate',
    campaignSortOrder: 1, //asc
  }

  filterCampaigns = (campaigns) => {
    var filtered = campaigns;

    var stateFilter = this.props.location.query.state || 'live';
    var typeFilter = this.props.location.query.type;

    if (stateFilter && stateFilter !== 'all') {
      filtered = filtered.filter((c) => c.status === stateFilter);
    }

    if (typeFilter) {
      filtered = filtered.filter((c) => c.type === typeFilter);
    }
    return filtered;
  }

  sortBy = (field, order, iteratees) => {
    let key = function(x) {return iteratees ? iteratees(x[field]) : x[field]};

    return function (a, b) {
      return a = key(a), b = key(b), order * ((a > b) - (b > a));
    }
  }

  prepareSortValues = (column, value) => {
    switch (column) {
      case 'name':
      case 'type':
      case 'status':
        return (value || "").toUpperCase();
      case 'actualValue':
      case 'startDate':
        return parseInt(value || 0, 10);
      case 'endDate':
        return value || Infinity;
    }

    return value;
  };

  sortCampaigns = (campaigns) => {
    let column = this.props.campaignSortColumn;
    let order = this.props.campaignSortOrder;

    return campaigns.sort(this.sortBy(column, order, this.prepareSortValues.bind(this, column)));
  }

  componentDidMount() {
    this.props.campaignActions.getCampaigns(this.props.territory);
    this.props.analyticsActions.getLatestAnalytics(this.props.territory);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.territory !== this.props.territory) {
      this.props.analyticsActions.getLatestAnalytics(nextProps.territory);
      this.props.campaignActions.getCampaigns(nextProps.territory);
    }
  }

  render() {

    return (
      <div className="campaigns">
        <h2 className="campaigns__header">Campaigns ({this.props.territory === 'global' ? 'global' : this.props.territory + ' only'})</h2>
        <CampaignList campaigns={this.filterCampaigns(this.sortCampaigns(this.props.campaigns))} latestAnalytics={this.props.latestAnalytics} />
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaigns from '../../actions/CampaignActions/getCampaigns';
import * as getLatestAnalytics from '../../actions/CampaignActions/getLatestAnalytics';

function mapStateToProps(state) {
  return {
    campaigns: state.campaigns,
    latestAnalytics: state.latestAnalytics,
    campaignSortColumn: state.campaignSort.campaignSortColumn,
    campaignSortOrder: state.campaignSort.campaignSortOrder,
    territory: state.territory
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignActions: bindActionCreators(Object.assign({}, getCampaigns), dispatch),
    analyticsActions: bindActionCreators(Object.assign({}, getLatestAnalytics), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Campaigns);
