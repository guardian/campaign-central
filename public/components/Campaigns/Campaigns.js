import React, {Component, PropTypes} from 'react';
import CampaignList from '../CampaignList/CampaignList';

class Campaigns extends Component {

  static propTypes = {
    campaigns: PropTypes.array.isRequired,
  }

  applyRouteBasedFiltering = (campaigns) => {
    const filterName = this.props.routeParams.filterName;

    switch (filterName) {

      case 'prospect':
        return campaigns.filter((c) => c.status === 'prospect');

      case 'production':
        return campaigns.filter((c) => c.status === 'production');

      case 'live':
        return campaigns.filter((c) => c.status === 'live');

      case 'dead':
        return campaigns.filter((c) => c.status === 'dead');

      default:
        return campaigns;
    }
  }

  applyCurrentFilters = (campaigns) => {
    return campaigns;
  }

  filterCampaigns = (campaigns) => {
    const routeFilteredCampaigns = this.applyRouteBasedFiltering(campaigns);
    return this.applyCurrentFilters(routeFilteredCampaigns);
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
    campaigns: state.campaigns
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignActions: bindActionCreators(Object.assign({}, getCampaigns), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Campaigns);
