import React from 'react';

export default class CampaignMediaEvents extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {
    return(
      <div className="campaign__row">
        <div className="campaign-box__header">Campaign media events</div>
          <div>{this.props.mediaEventsData.campaignName}</div>
      </div>
    );

  }
}
