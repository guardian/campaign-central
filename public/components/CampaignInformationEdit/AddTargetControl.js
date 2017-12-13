import React, { PropTypes } from 'react';
import EditableNumber from '../utils/EditableNumber';
import EditableText from '../utils/EditableText';
import { defaultTargets } from '../../constants/defaultTargets';
import R from 'ramda';

class AddTargetControl extends React.Component {

  static propTypes = {
    existingTargets: PropTypes.object.isRequired,
    onTargetAdded: PropTypes.func.isRequired
  };

  static defaultProps = {
    target: undefined,
    value: null
  };

  state = {};

  triggerAdd = () => {
    this.props.onTargetAdded(this.state.target, this.state.territory, this.state.value);
    this.setState({
      selectedTarget: undefined,
      target: undefined,
      territory: undefined,
      value: null
    });
  }

  updateSelectedMetric = (e) => {
    const selected = e.target.value;
    this.setState({selectedTarget: selected, target: selected});
  }

  updateTarget = (e) => {
    this.setState({target: e.target.value});
  }

  updateTargetValue = (v) => {
    this.setState({value: v});
  }

  updateTerritory = (e) => {
    const selected = e.target.value;
    this.setState({territory: selected});
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

    const targetsToExclude = Object.entries(this.props.existingTargets).filter(([key, value]) =>
      R.equals(['AU', 'GB', 'US', 'global'], Array.sort(Object.keys(value)))
    ).map(([key, value]) => key);

    const targetsToRender = defaultTargets.filter(t => !targetsToExclude.includes(t.value));

    return (
      <div>
        Target: <select value={this.state.selectedTarget} onChange={this.updateSelectedMetric} style={{marginRight: '10px'}}>
          <option value=""></option>
          { targetsToRender.map((t) => <option key={t.value} value={t.value}>{t.name}</option>) }
        </select>
        Territory: <select id="territoryDropdown" style={{marginRight: '10px'}} onChange={this.updateTerritory}>
                          <option value=""></option>
                          <option value="global">global</option>
                          <option value="GB">uk</option>
                          <option value="US">us</option>
                          <option value="AU">au</option>
                        </select>
        Count: <EditableNumber value={this.state.value} onNumberChange={this.updateTargetValue} />
        {this.renderAddButton()}
      </div>
    );
  }
}

export default AddTargetControl;
