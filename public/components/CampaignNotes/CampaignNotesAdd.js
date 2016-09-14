import React, {Component, PropTypes} from 'react';

class CampaignNotesAdd extends Component {

  static propTypes = {
      id: PropTypes.string.isRequired,
      onSave: PropTypes.func.isRequired
  };

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
      this.props.onSave(this.props.id, { content: this.state.message });
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

export default CampaignNotesAdd;

