import React, {Component, PropTypes} from 'react';

class CampaignNotesAdd extends Component {

  state = {
    adding: false
  }

  enableAdding = () => {

    window.addEventListener('click', this.disableAdding, true);

    this.setState({
      adding: true
    });
  }

  disableAdding = (event) => {

    if (event.target !== this.refs.addNote && event.target !== this.refs.submitNote) {
      window.removeEventListener('click', this.disableAdding, true);

      this.setState({
        adding: false
      });
    }
  }

  componentWillUnmount() {
    window.removeEventListener('click', this.disableAdding, true);
  }

  updateNote = (event) => {
    this.setState({ message: event.target.value });

  }

  saveNote = () => {
      this.props.campaignNotesAddActions.createNote(this.props.id, { content: this.state.message });
      window.removeEventListener('click', this.disableAdding, true);

      this.setState({
        adding: false
      });
  }

  render() {

    if (!this.state.adding) {
      return (
        <button onClick={this.enableAdding}>Add a note</button>
      );
    }

    if (this.state.adding) {
        return (
          <div ref="parent">
            <textarea ref="addNote" onChange={this.updateNote.bind(this)} placeholder="Add a new note"></textarea>
            <button ref="submitNote" onClick={this.saveNote}>submit</button>
          </div>
        );
    }

  };
}
//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createNote from '../../actions/CampaignActions/createCampaignNote';

function mapStateToProps(state) {
  return {
      message: state.message
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignNotesAddActions: bindActionCreators(Object.assign({}, createNote), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignNotesAdd);

