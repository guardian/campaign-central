import React, {Component, PropTypes} from 'react';
import CampaignList from '../CampaignList/CampaignList';

class Campaigns extends Component {

  static propTypes = {
    campaigns: PropTypes.array.isRequired,
    activeFilters: PropTypes.object.isRequired
  }

  filterCampaigns(campaigns) {
    return [];
  }

  componentDidMount() {
    this.props.campaignActions.getCampaigns();
  }

  render() {
    return (
      <div className="campaigns">
        <div className="campaigns__sidebar">

        </div>
        <div className="campaigns__body">
          <h2 className="campaigns__header">Campaigns</h2>
          <CampaignList campaigns={this.props.campaigns} />
        </div>
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
