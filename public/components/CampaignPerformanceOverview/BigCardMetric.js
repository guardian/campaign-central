import React, { PropTypes } from 'react';
import Modal from 'react-awesome-modal';
import MetricBreakdownTable from './MetricBreakdownTable';

export default class BigCardMetric extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
        visible : false
    }
  }

  openModal() {
      this.setState({
          visible : true
      });
  }

  closeModal() {
    this.setState({
        visible : false
    });
  }

  renderOpenModalIcon() {
      if (this.props.metricByPath || this.props.metricByDevice) {
        return(<i className="i-plus align-icon-right" onClick={() => this.openModal()} />);
      } else {
        return(null);
      }
  }

  render() {

    return (
      <div className="box">
          {this.renderOpenModalIcon()}
          <div className="head">{this.props.metricLabel}</div>
          <div className="count ">{this.props.metricValue}</div>
          <div className="unit">{this.props.metricUnit ? this.props.metricUnit : this.props.metricTargetMessage}</div>
          <Modal
              visible={this.state.visible}
              width="90%"
              height="90%"
              effect="fadeInUp"
              onClickAway={() => this.closeModal()}>
              <div style={{padding:'50px'}}>

              <i className="i-cross align-icon-top-right-in-modal" onClick={() => this.closeModal()} />

              {this.props.metricByPath ?
                <MetricBreakdownTable tableHeaders={['Path', this.props.metricLabel]}
                                      tableValues={this.props.metricByPath}
                                      metricUnit={this.props.metricUnit} />
                                      : null
              }

              {this.props.metricByDevice ?
                <MetricBreakdownTable tableHeaders={['Path', this.props.metricLabel]}
                                      tableValues={this.props.metricByDevice}
                                      metricUnit={this.props.metricUnit} />
                                      : null
              }

              </div>
          </Modal>
      </div>
    );
  }
}
