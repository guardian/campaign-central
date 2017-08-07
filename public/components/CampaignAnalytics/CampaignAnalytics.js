import React, {PropTypes} from "react";
import CampaignUniquesChart from './Analytics/CampaignUniquesChart';

class CampaignAnalytics extends React.Component {

  render () {

    if(!this.props.campaignUniques || !this.props.campaignUniques.length > 0) return null;

    return (
      <CampaignUniquesChart data={this.props.campaignUniques}/>
    );
  }
}


//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';

function mapStateToProps(state) {
  return {
    campaignUniques: state.campaignUniques,
  };
}

export default connect(mapStateToProps)(CampaignAnalytics);
