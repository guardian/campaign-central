import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';

class Clients extends Component {

  static propTypes = {
    clients: PropTypes.array.isRequired,
  }

  componentDidMount() {
    this.props.clientActions.getClients();
  }

  render() {

    return (
      <div className="campaigns">
        <h2 className="campaigns__header">Clients</h2>
        {this.props.clients.map(c => <Link to={"/clients/" + c.id}>{c.name}</Link>)}
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getClients from '../../actions/ClientActions/getClients';

function mapStateToProps(state) {
  return {
    clients: state.clients
  };
}

function mapDispatchToProps(dispatch) {
  return {
    clientActions: bindActionCreators(Object.assign({}, getClients), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Clients);
