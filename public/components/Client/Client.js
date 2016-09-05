import React, { PropTypes } from 'react';

class Campaign extends React.Component {

  static propTypes = {
    client: PropTypes.object.isRequired
  }

  componentWillMount() {
    this.props.clientActions.getClient(this.props.params.id);
  }

  render () {
    if (!this.props.client) {
      return <div>Loading... </div>;
    }

    return (
      <div className="client">
        <h2>{this.props.client.name}</h2>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getClient from '../../actions/ClientActions/getClient';

function mapStateToProps(state) {
  return {
    client: state.client
  };
}

function mapDispatchToProps(dispatch) {
  return {
    clientActions: bindActionCreators(Object.assign({}, getClient), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Campaign);
