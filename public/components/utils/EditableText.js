import React, { PropTypes } from 'react';

class EditableText extends React.Component {

  static propTypes = {
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired
  };

  static defaultProps = {
    value: ""
  }

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
        <input className="editable-text__input" value={this.props.value} onChange={this.props.onChange} />
        <div className="editable-text__button" onClick={this.disableEditing} >
          <i className="i-cross-grey"/>
        </div>
      </div>
    );

  }
}



export default EditableText;
