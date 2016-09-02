import React, { PropTypes } from 'react';

class EditableDropdown extends React.Component {

  static propTypes = {
    name: PropTypes.string.isRequired,
    values: PropTypes.object.isRequired,
    selectedValue: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired
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

  prepareOptions = () => {
    const values = this.props.values;
    let options = [];

    for (let prop in values) {
      if (values.hasOwnProperty(prop)) {
        options.push(<option value={prop} key={prop}>{values[prop]}</option>);
      }
    }

    return options;
  }

  render () {
    if (!this.state.editable) {
      return (
        <div className="editable-text" onClick={this.enableEditing} >
          <span className={"editable-text__text"}>{this.props.values[this.props.selectedValue]}</span>
          <div className="editable-text__button" >
            <i className="i-pen"/>
          </div>
        </div>
      );
    }

    return (
      <div className="editable-text">
        <select name={this.props.name} value={this.props.selectedValue} onChange={this.props.onChange}>
          {this.prepareOptions()}
        </select>
        <div className="editable-text__button" onClick={this.disableEditing} >
           <i className="i-cross-grey"/>
        </div>
      </div>
    );

  }
}



export default EditableDropdown;
