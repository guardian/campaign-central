import React, { PropTypes } from 'react';
import {Link} from 'react-router';

class Sidebar extends React.Component {

  setCampaignStateFilter = (f) => {
    this.props.uiActions.setCampaignStateFilter(f);
  };

  setCampaignTypeFilter = (f) => {
    this.props.uiActions.setCampaignTypeFilter(f);
  };

  stateFilterLink = (stateValue, displayName) => {
    var className = 'sidebar__filter-group__link';

    if(this.props.campaignStateFilter === stateValue) {
      className = className + ' sidebar__filter-group__link--active'
    }

    return(
      <Link to="/campaigns" className={className} onClick={() => this.setCampaignStateFilter(stateValue)}>{displayName}</Link>
    )
  };

  typeFilterLink = (stateValue, displayName) => {
    var className = 'sidebar__filter-group__link';

    if(this.props.campaignTypeFilter === stateValue) {
      className = className + ' sidebar__filter-group__link--active'
    }

    return(
      <Link to="/campaigns" className={className} onClick={() => this.setCampaignTypeFilter(stateValue)}>{displayName}</Link>
    )
  };

  render () {
    return (
      <div className="sidebar">
        <div className="sidebar__link-group">
          <div className="sidebar__link-group__header">Campaigns</div>
          <SidebarLink to="/campaigns">All Campaigns</SidebarLink>
          <div className="sidebar__filter-group">
            <div className="sidebar__filter-group__header">State:</div>
            {this.stateFilterLink(false, 'All')}
            {this.stateFilterLink('prospect', 'Prospects')}
            {this.stateFilterLink('production', 'In Production')}
            {this.stateFilterLink('live', 'Live')}
            {this.stateFilterLink('dead', 'Dead')}
          </div>
          <div className="sidebar__filter-group">
            <div className="sidebar__filter-group__header">Type:</div>
            {this.typeFilterLink(false, 'All')}
            {this.typeFilterLink('hosted', 'Hosted')}
            {this.typeFilterLink('paidContent', 'Paid Content')}
          </div>
        </div>
        <div className="sidebar__link-group">
          <div className="sidebar__link-group__header">Clients</div>
          <SidebarLink to="/clients">All Clients</SidebarLink>
        </div>
        <div className="sidebar__link-group">
          <div className="sidebar__link-group__header">Tools</div>
          <SidebarLink to="/capiImport">Import campaign</SidebarLink>
        </div>
      </div>
    );
  }
}

class SidebarLink extends React.Component {
  render () {
    return <Link
      to={this.props.to}
      className="sidebar__link"
      activeClassName="sidebar__link--active">
        {this.props.children}
    </Link>;
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as setCampaignStateFilter from '../../actions/UIActions/setCampaignStateFilter';
import * as setCampaignTypeFilter from '../../actions/UIActions/setCampaignTypeFilter';

function mapStateToProps(state) {
  return {
    campaignStateFilter: state.campaignStateFilter,
    campaignTypeFilter: state.campaignTypeFilter
  };
}

function mapDispatchToProps(dispatch) {
  return {
    uiActions: bindActionCreators(Object.assign({}, setCampaignStateFilter, setCampaignTypeFilter), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Sidebar);
