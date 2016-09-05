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
    this.setState({
      editable: true
    });
  }

  disableEditing = () => {
    this.setState({
      editable: false
    });
  }

  updateValue = (e) => {
    const fieldValue = e.target.value;

    if(parseInt(fieldValue) !== NaN) {
      this.props.onNumberChange(parseInt(fieldValue));
    }
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
        <input className="editable-text__input" value={this.props.value || ""} onChange={this.updateValue} />
        <div className="editable-text__button" onClick={this.disableEditing} >
          <i className="i-cross-grey"/>
        </div>
      </div>
    );

  }
}



export default EditableNumber;
