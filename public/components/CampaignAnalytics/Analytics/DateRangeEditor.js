import React from 'react';

class DateRangeEditor extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      startDate: DateRangeEditor.toDate(this.props.campaign.startDate),
      endDate: DateRangeEditor.toDate(this.props.campaign.endDate)
    };
    this.handleChange = this.handleChange.bind(this);
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState !== this.state) {
      this.props.onChange(nextState);
    }
  }

  handleChange({target}) {
    this.setState({
      [target.name]: target.value
    });
  }

  static zeroPad(num) {
    return ('0' + num).slice(-2)
  }

  static toDate(epochMillis) {
    let date = new Date(epochMillis);
    return date.getFullYear() + '-' + DateRangeEditor.zeroPad(date.getMonth() + 1) + '-' + DateRangeEditor.zeroPad(date.getDate());
  }

  render() {
    return (
      <div className="campaign-referral-daterange">
        <label>From&nbsp;
          <input type="date" name="startDate" defaultValue={this.state.startDate} onChange={this.handleChange}
                 min={DateRangeEditor.toDate(this.props.campaign.startDate)}
                 max={DateRangeEditor.toDate(this.props.campaign.endDate)}
                 className="campaign-referral-daterange-field"/>
        </label>
        <label>To&nbsp;
          <input type="date" name="endDate" defaultValue={this.state.endDate} onChange={this.handleChange}
                 min={DateRangeEditor.toDate(this.props.campaign.startDate)}
                 max={DateRangeEditor.toDate(this.props.campaign.endDate)}
                 className="campaign-referral-daterange-field"/>
        </label>
      </div>
    );
  }
}

export default DateRangeEditor;
