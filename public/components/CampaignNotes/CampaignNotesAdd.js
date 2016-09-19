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

    window.addEventListener('click', this.saveNote, true);

    this.setState({
      adding: true
    });

    if (this.props.content) {
      this.setState({
        content: this.props.content
      });
    }
  }

  saveNote = (event) => {

    if (this.state.content) {
      this.props.onSave(this.props.id, { content: this.state.content, created: this.props.created });
    }

    if (event.target !== this.refs.addNote && event.target !== this.refs.submitNote) {
      window.removeEventListener('click', this.saveNote, true);

      this.setState({
        adding: false
      });
    }
  }

  componentWillUnmount() {
    window.removeEventListener('click', this.saveNote, true);
  }

  updateNote = (event) => {
    this.setState({ content: event.target.value });

  }

  render() {

    if (!this.state.adding && this.props.content) {
      return <div className="note-container" onClick={this.enableAdding}>{this.props.content}</div>;
    }

    if (!this.state.adding) {
      return (
        <div className="note-add" onClick={this.enableAdding}><i title="Add a new note" className="i-pen"/></div>
      );
    }

    if (!this.props.content) {
      return (
        <div ref="parent">
          <textarea className="note-input" ref="addNote" onChange={this.updateNote.bind(this)} placeholder="Add a new note"></textarea>
        </div>
      );
    }
    return (
      <div ref="parent">
        <textarea className="note-input" ref="addNote" onChange={this.updateNote.bind(this)} value={this.state.content}></textarea>
      </div>
    );
  };
}

export default CampaignNotesAdd;
