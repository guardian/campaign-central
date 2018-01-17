import React from 'react';
import { Route } from 'react-router-dom';

import Header from './Header/Header';
import Sidebar from './Sidebar/Sidebar';
import Campaigns from './Campaigns/Campaigns';
import Benchmarks from './Campaigns/Benchmarks';
import Campaign from './Campaign/Campaign';
import { Glossary } from './Glossary/Glossary';


class Main extends React.Component {

  clearError = () => {
    this.props.uiActions.clearError();
  }

  renderErrorBar = () => {
    if (!this.props.error) {
      return false;
    }

    return (
      <div className="error-bar">
        {this.props.error || 'An error has occured, please refresh your browser. If this problem persists please contact Central Production'}
        <span className="error-bar__dismiss" onClick={this.clearError}>
          <i className="i-cross-grey"></i>
        </span>
      </div>
    );
  }

  render () {
    return (
      <div className="main">
        <Header />
        {this.renderErrorBar()}
        <div className="main__flex-container">
          <div className="main__sidebar">
            <Sidebar search={this.props.location.search}/>
          </div>
          <div className="main__content">
            <div>
              <Route path="/campaigns" component={Campaigns} />
              <Route path="/benchmarks" component={Benchmarks} />
              <Route path="/campaigns/:filterName" component={Campaigns} />
              <Route path="/campaign/:id" component={Campaign} />
              <Route path="/glossary" component={Glossary} />
            </div>
          </div>
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as clearError from '../actions/UIActions/clearError';

function mapStateToProps(state) {
  return {
    error: state.error
  };
}

function mapDispatchToProps(dispatch) {
  return {
    uiActions: bindActionCreators(Object.assign({}, clearError), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Main);
