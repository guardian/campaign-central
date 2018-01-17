import React, { PropTypes } from 'react';
import {NavLink} from 'react-router-dom';
import Moment from 'moment';

class Sidebar extends React.Component {

  componentDidMount() {
    this.props.reportExecutionActions.getLastExecuted(this.props.territory);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.territory !== this.props.territory) {
      this.props.reportExecutionActions.getLastExecuted(nextProps.territory);
    }
  }

  filterLink = (searchValues, displayName) => {
    const highlightLink = () => {
      const currentSearchParams = new URLSearchParams(this.props.search);

      for (let key of currentSearchParams.keys()) {
        if (searchValues[key] === currentSearchParams.get(key)) {
          return true;
        }
      }

      for (let key in searchValues) {
        if (searchValues.hasOwnProperty(key)) {
          if (searchValues[key] === undefined && !currentSearchParams.has(key)) {
            return true;
          }
        }
      }

      return false;
    };

    const searchParams = new URLSearchParams(this.props.search);

    for (let param in searchValues) {
      if (searchValues.hasOwnProperty(param)) {
        if (searchValues[param]) {
          searchParams.set(param, searchValues[param]);
        } else {
          searchParams.delete(param);
        }
      }
    }

    return(
      <NavLink to={{pathname: "/campaigns", search: `?${searchParams.toString()}`}}
               className="sidebar__filter-group__link"
               isActive={highlightLink}
               activeClassName="sidebar__filter-group__link--active">
        {displayName}
      </NavLink>
    )
  };

  onTerritoryChange = (e) => {
    let territory = e.target.value;
    this.props.uiActions.setTerritory(territory);
  }

  renderDataLastRetrievedLabel = () => {
    if (!this.props.lastExecutedDateTime) return null;

    const offset = new Date(this.props.lastExecutedDateTime.lastExecuted);

    if (offset === 0) {
      return ( `Data last updated ${Moment(this.props.lastExecutedDateTime.lastExecuted).fromNow()}` );
    } else if (Math.sign(offset) === 1) {
      return ( `Data last updated ${Moment(this.props.lastExecutedDateTime.lastExecuted).subtract(offset, 'minutes').fromNow()}` );
    } else {
      return ( `Data last updated ${Moment(this.props.lastExecutedDateTime.lastExecuted).add(offset, 'minutes').fromNow()}` );
    }
  }

  render () {

    return (
      <div className="sidebar">
        <div className="sidebar__link-group">
        <div className="sidebar__link-group__header">Select a territory:</div>
          <form className="pure-form pure-form-stacked">
            <div className="pure-u-1 pure-u-md-1-5 territory-container">
              <select id="territoryDropdown" className="territory-dropdown" onChange={(e) => this.onTerritoryChange(e)} value={this.props.territory}>
                  <option value="GB">uk</option>
                  <option value="US">us</option>
                  <option value="AU">au</option>
                  <option value="global">global</option>
              </select>
            </div>
          </form>

          <div className="sidebar__link-group__header">Campaigns</div>
          <SidebarLink to="/benchmarks">Benchmarks</SidebarLink>
          <SidebarLink to="/campaigns">All Campaigns</SidebarLink>
          <div className="sidebar__filter-group">
            <div className="sidebar__filter-group__header">State:</div>
            {this.filterLink({state: 'all'}, 'All')}
            {this.filterLink({state: undefined}, 'Live')}
            {this.filterLink({state: 'dead'}, 'Finished')}
          </div>
          <div className="sidebar__filter-group">
            <div className="sidebar__filter-group__header">Type:</div>
            {this.filterLink({type: undefined}, 'All')}
            {this.filterLink({type: 'hosted'}, 'Hosted')}
            {this.filterLink({type: 'paidcontent'}, 'Paid Content')}
          </div>
        </div>
        <div className="sidebar__link-group">
          <div className="sidebar__link-group__header">Tools</div>
          <SidebarLink to="/glossary">Glossary</SidebarLink>
        </div>
        <div className="sidebar__link-group">
          <div className="sidebar__data__last__updated">{this.renderDataLastRetrievedLabel()}</div>
        </div>
      </div>
    );
  }
}

class SidebarLink extends React.Component {
  render () {
    return <NavLink
      to={this.props.to}
      className="sidebar__link"
      activeClassName="sidebar__link--active">
        {this.props.children}
    </NavLink>;
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as setTerritory from '../../actions/UIActions/setTerritory';
import * as getLastExecuted from '../../actions/ReportExecutionActions/getLastExecuted';

function mapStateToProps(state) {
  return {
    territory: state.territory,
    lastExecutedDateTime: state.lastExecuted
  };
}

function mapDispatchToProps(dispatch) {
  return {
    uiActions: bindActionCreators(Object.assign({}, setTerritory), dispatch),
    reportExecutionActions: bindActionCreators(Object.assign({}, getLastExecuted), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Sidebar);
