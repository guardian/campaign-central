import React, { PropTypes } from 'react';
import {Link} from 'react-router';

class Sidebar extends React.Component {

  render () {
    return (
      <div className="sidebar">
        <div className="sidebar__link-group">
          <div className="sidebar__link-group__header">Campaigns</div>
          <SidebarLink to="/campaigns">All Campaigns</SidebarLink>
          <SidebarLink to="/campaigns/prospects">Prospects</SidebarLink>
          <SidebarLink to="/campaigns/production">In Production</SidebarLink>
          <SidebarLink to="/campaigns/active">Active</SidebarLink>
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


export default Sidebar;
