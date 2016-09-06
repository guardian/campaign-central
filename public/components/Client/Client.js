import React, { PropTypes } from 'react';
import ClientInformationEdit from '../ClientInformationEdit/ClientInformationEdit';

class Client extends React.Component {

  static propTypes = {
    client: PropTypes.object
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
        <ClientInformationEdit client={this.props.client} updateClient={this.props.clientActions.updateClient} saveClient={this.props.clientActions.saveClient}/>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getClient from '../../actions/ClientActions/getClient';
import * as updateClient from '../../actions/ClientActions/updateClient';
import * as saveClient from '../../actions/ClientActions/saveClient';

function mapStateToProps(state) {
  return {
    client: state.client
  };
}

function mapDispatchToProps(dispatch) {
  return {
    clientActions: bindActionCreators(Object.assign({}, getClient, updateClient, saveClient), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Client);
