import React, { PropTypes } from 'react';
import EditableText from '../utils/EditableText';

class ClientInformationEdit extends React.Component {

  static propTypes = {
    client: PropTypes.object,
    updateClient: PropTypes.func
  }

  state = {
    isCampaignDirty: false
  }

  triggerSave = () => {
    this.props.saveClient(this.props.client.id, this.props.client);
    this.setState({
      isClientDirty: false
    });
  }

  triggerUpdate = (newClient) => {
    this.setState({
      isClientDirty: true
    });

    this.props.updateClient(newClient.id, newClient);
  }

  updateClientName = (e) => {
    this.triggerUpdate(Object.assign({}, this.props.client, {
      name: e.target.value
    }));
  }

  updateClientCountry = (e) => {
    this.triggerUpdate(Object.assign({}, this.props.client, {
      country: e.target.value
    }));
  }

  renderSaveButtons = () => {
    if (!this.state.isClientDirty) {
      return false;
    }

    return (
      <div className="campaign-box__footer">
        <span className="campaign-info__button" onClick={this.triggerSave}>Save</span>
      </div>
    );
  }

  render () {

    return (
      <div className="client-info campaign-box">
        <div className="campaign-box__header">
          Client Info
        </div>
        <div className="campaign-box__body">
          <div className="campaign-info__field">
            <label>Name</label>
            <EditableText value={this.props.client.name} onChange={this.updateClientName} />
          </div>
          <div className="campaign-info__field">
            <label>Country</label>
            <EditableText value={this.props.client.country} onChange={this.updateClientCountry} />
          </div>

        </div>
        {this.renderSaveButtons()}
      </div>
    );
  }
}

export default ClientInformationEdit;
