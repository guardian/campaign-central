import React from 'react';
import Header from './Header/Header';
import Sidebar from './Sidebar/Sidebar';

class Main extends React.Component {

  static propTypes = {
    children: React.PropTypes.element.isRequired
  }

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
        <div className="main__sidebar">
          <Sidebar />
        </div>
        <div className="main__content">
          {this.props.children}
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
