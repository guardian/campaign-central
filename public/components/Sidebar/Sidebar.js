import React, { PropTypes } from 'react';
import {Link} from 'react-router';

export default class Sidebar extends React.Component {

  filterLink = (changeValue, displayName) => {
    var query = Object.assign({}, this.props.query, changeValue);

    return(
      <Link to={{pathname: "/campaigns", query: query}} className="sidebar__filter-group__link" activeClassName="sidebar__filter-group__link--active">{displayName}</Link>
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
            {this.filterLink({state: 'all'}, 'All')}
            {this.filterLink({state: 'prospect'}, 'Prospects')}
            {this.filterLink({state: 'production'}, 'In Production')}
            {this.filterLink({state: undefined}, 'Live')}
            {this.filterLink({state: 'dead'}, 'Dead')}
          </div>
          <div className="sidebar__filter-group">
            <div className="sidebar__filter-group__header">Type:</div>
            {this.filterLink({type: undefined}, 'All')}
            {this.filterLink({type: 'hosted'}, 'Hosted')}
            {this.filterLink({type: 'paidContent'}, 'Paid Content')}
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
