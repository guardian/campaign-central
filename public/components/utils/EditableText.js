import React from 'react';
import PropTypes from 'prop-types';

class EditableText extends React.Component {

  static propTypes = {
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    error: PropTypes.string
  };

  static defaultProps = {
    value: ""
  }

  state = {
    editable: false
  }

  enableEditing = () => {
    window.addEventListener('click', this.disableEditing, true);

    this.setState({
      editable: true
    });
  }

  disableEditing = (event) => {
    if (event.target !== this.refs.editableInput) {
        window.removeEventListener('click', this.disableEditing, true);

        this.setState({
            editable: false
        });
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
        <input ref="editableInput" className="editable-text__input" value={this.props.value || ""} onChange={this.props.onChange} />
        <span className="campaign-info__field__error">{this.props.error}</span>
      </div>
    );
  }
}



export default EditableText;
