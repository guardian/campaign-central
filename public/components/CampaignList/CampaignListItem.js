import React, { PropTypes } from 'react';

class CapaignListItem extends React.Component {

  static propTypes = {
    campaign: PropTypes.shape({
      name: PropTypes.string,
      id: PropTypes.string
    }).isRequired
  };

  render () {
    return (
      <div className="campaign-list__item">
        {this.props.campaign.name}
      </div>
    );
  }
}

export default CapaignListItem;
