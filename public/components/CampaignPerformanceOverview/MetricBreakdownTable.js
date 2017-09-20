import React, { PropTypes } from 'react';

export default class MetricBreakdownTable extends React.Component {

  render() {

    const headers = this.props.tableHeaders.map(header => <th key={header}>{header}</th>);

    const values = Object.keys(this.props.tableValues).sort().map(key => {
      return (
        <tr key={key}>
            <td>{key}</td>
            <td>{this.props.tableValues[key]} {this.props.metricUnit}</td>
        </tr>
      );
    });

    return(
      <table className="pure-table" style={{margin:'auto'}}>
          <thead>
              <tr>
                  {headers}
              </tr>
          </thead>

          <tbody>
            {values}
          </tbody>
      </table>
    );
  }

}
