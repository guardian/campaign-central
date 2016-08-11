import React, {Component, PropTypes} from 'react';

class Campaigns extends Component {

  static propTypes = {
    campaigns: PropTypes.array.isRequired,
    activeFilters: PropTypes.object.isRequired
  }

  filterCampaigns(campaigns) {
    return [];
  }

  render() {
    return (
      <div className="campaigns">
        <div className="campaigns__sidebar">
        </div>
        <div className="campaigns__body">
        </div>
      </div>
    );
  }
}

export default Campaigns;
