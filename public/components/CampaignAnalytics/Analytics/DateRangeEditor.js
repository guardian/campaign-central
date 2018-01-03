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
    let dataThreshold = 1504220400000; // 1 Sep 2017
    let today = Date.now();
    let startDate = DateRangeEditor.toDate(Math.max(dataThreshold, Date.parse(this.state.startDate)));
    let endDate = DateRangeEditor.toDate(Math.min(today, Date.parse(this.state.endDate)));
    let minDate = DateRangeEditor.toDate(Math.max(dataThreshold, this.props.campaign.startDate));
    let maxDate = DateRangeEditor.toDate(Math.min(today, this.props.campaign.endDate));
    return (
      <div className="campaign-referral-daterange">
        <label>From&nbsp;
          <input type="date"
                 name="startDate"
                 defaultValue={startDate}
                 onChange={this.handleChange}
                 min={minDate}
                 max={maxDate}
                 className="campaign-referral-daterange-field"/>
        </label>
        <label>To&nbsp;
          <input type="date"
                 name="endDate"
                 defaultValue={endDate}
                 onChange={this.handleChange}
                 min={minDate}
                 max={maxDate}
                 className="campaign-referral-daterange-field"/>
        </label>
      </div>
    );
  }
}

export default DateRangeEditor;
