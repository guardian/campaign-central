import React, { PropTypes } from 'react';

class EditableDropdown extends React.Component {

  static propTypes = {
    name: PropTypes.string.isRequired,
    values: PropTypes.array.isRequired,
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

    values.map((prop, index) =>
      options.push(<option value={prop.value} key={index}>{prop.name}</option>)
    );

    return options;
  }

  isMatchingValue = (obj) => {
      return obj.value === this.props.selectedValue;
  }

  render () {
    const matchedValue = this.props.values.filter(this.isMatchingValue);

    if (!this.state.editable && matchedValue.length) {
      return (
        <div className="editable-text" onClick={this.enableEditing} >
          <span className={"editable-text__text"}>{matchedValue[0].name}</span>
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
