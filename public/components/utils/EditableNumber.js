import React, { PropTypes } from 'react';

class EditableNumber extends React.Component {

  static propTypes = {
    value: PropTypes.number,
    onNumberChange: PropTypes.func.isRequired
  };

  state = {
    editable: false
  }

  enableEditing = () => {

    window.addEventListener('click', this.disableEditing, true);

    this.setState({
      editable: true
    });
  }

  disableEditing = () => {
    if (event.target !== this.refs.editableInput) {

      window.removeEventListener('click', this.disableEditing, true);

      this.setState({
        editable: false
      });

    }
  }

  updateValue = (e) => {
    const fieldValue = e.target.value;

    if(parseInt(fieldValue) !== NaN) {
      this.props.onNumberChange(parseInt(fieldValue));
    }
  }

  componentWillUnmount() {
    window.removeEventListener('click', this.disableEditing, true);
  }

  render () {

    if (!this.state.editable) {
      return (
        <div className="editable-text" onClick={this.enableEditing} >
          <span className={this.props.value ? "editable-text__text" : "editable-text__text--empty"}>{this.props.value || "Empty"}</span>
          <div className="editable-text__button" >
            <i className="i-pen"/>
          </div>
        </div>
      );
    }

    return (
      <div className="editable-text">
        <input ref="editableInput" className="editable-text__input" value={this.props.value || ""} onChange={this.updateValue} />
      </div>
    );

  }
}



export default EditableNumber;
