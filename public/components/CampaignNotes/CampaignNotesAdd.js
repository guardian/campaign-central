import React, {Component, PropTypes} from 'react';

class CampaignNotesAdd extends Component {

  static propTypes = {
    id: PropTypes.string.isRequired,
    onSave: PropTypes.func.isRequired,
    content: PropTypes.string
  };

  state = {
    adding: false
  }

  enableAdding = () => {

    window.addEventListener('click', this.disableAdding, true);

    this.setState({
      adding: true
    });

    if (this.props.content) {
      this.setState({
        content: this.props.content
      });
    }
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
    this.setState({ content: event.target.value });

  }

  saveNote = () => {
    this.props.onSave(this.props.id, { content: this.state.content, created: this.props.created });
    window.removeEventListener('click', this.disableAdding, true);

    this.setState({
      adding: false
    });
  }

  render() {

    if (!this.state.adding && this.props.content) {
      return <div onClick={this.enableAdding}>{this.props.content}</div>;
    }

    if (!this.state.adding) {
      return (
        <button onClick={this.enableAdding}>Add a note</button>
      );
    }

    if (!this.props.content) {
      return (
        <div ref="parent">
          <textarea ref="addNote" onChange={this.updateNote.bind(this)} placeholder="Add a new note"></textarea>
          <button ref="submitNote" onClick={this.saveNote}>submit</button>
        </div>
      );
    }
    return (
      <div ref="parent">
        <textarea ref="addNote" onChange={this.updateNote.bind(this)} value={this.state.content}></textarea>
        <button ref="submitNote" onClick={this.saveNote}>submit</button>
      </div>
    );
  };
}

export default CampaignNotesAdd;
