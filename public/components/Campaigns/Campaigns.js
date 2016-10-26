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

  componentDidMount() {
    this.props.campaignActions.getCampaigns();
  }

  render() {

    return (
      <div className="campaigns">
        <h2 className="campaigns__header">Campaigns</h2>
        <CampaignList campaigns={this.filterCampaigns(this.props.campaigns)} />
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaigns from '../../actions/CampaignActions/getCampaigns';

function mapStateToProps(state) {
  return {
    campaigns: state.campaigns,
    campaignStateFilter: state.campaignStateFilter,
    campaignTypeFilter: state.campaignTypeFilter
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignActions: bindActionCreators(Object.assign({}, getCampaigns), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Campaigns);
