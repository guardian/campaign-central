import React, { PropTypes } from 'react';
import R from 'ramda';
import EditableNumber from '../utils/EditableNumber';
import EditableText from '../utils/EditableText';

import { defaultTargets } from '../../constants/defaultTargets'

class AddTargetControl extends React.Component {

  static propTypes = {
    existingTargets: PropTypes.arrayOf(PropTypes.string).isRequired,
    onTargetAdded: PropTypes.func.isRequired
  };

  static defaultProps = {
    target: undefined,
    value: null
  }

  state = {
    isTargetValid: false
  }

  triggerAdd = () => {
    this.props.onTargetAdded(this.state.target, this.state.value);
    this.setState({
      isTargetValid: false,
      selectedTarget: undefined,
      target: undefined,
      value: null
    });
  }

  updateSelectedMetric = (e) => {
    const selected = e.target.value;

    if (selected === 'custom') {
      this.setState({selectedTarget: selected, target: undefined});
    } else {
      this.setState({selectedTarget: selected, target: selected});
    }
  }

  updateTarget = (e) => {
    this.setState({target: e.target.value});
  }

  updateTargetValue = (v) => {
    this.setState({value: v});
  }

  renderAddButton = () => {
    if (!this.state.target || !this.state.value) {
      return;
    }

    return (
      <div className="editable-text__button" onClick={this.triggerAdd} >
      <i className="i-plus"/>
    </div>
    );
  }
  
  render () {

    const availbleTargets = defaultTargets.filter(t => !this.props.existingTargets.includes(t.value) );

    if (this.state.selectedTarget === 'custom') {
      return (
        <div>
          <select value={this.state.selectedTarget} onChange={this.updateSelectedMetric}>
            <option value=""></option>
            { availbleTargets.map((t) => <option key={t.value} value={t.value}>{t.name}</option>) }
            <option value="custom">Custom</option>
          </select>
          <br/>
          Target: <EditableText value={this.state.target} onChange={this.updateTarget} />
          Count: <EditableNumber value={this.state.value} onNumberChange={this.updateTargetValue} />
          {this.renderAddButton()}
        </div>
      )
    }

    return (
      <div>
        <select value={this.state.selectedTarget} onChange={this.updateSelectedMetric}>
          <option value=""></option>
          { availbleTargets.map((t) => <option key={t.value} value={t.value}>{t.name}</option>) }
          <option value="custom">Custom</option>
        </select>
        &nbsp;Count: <EditableNumber value={this.state.value} onNumberChange={this.updateTargetValue} />
        {this.renderAddButton()}
      </div>
    );
  }
}

export default AddTargetControl;
