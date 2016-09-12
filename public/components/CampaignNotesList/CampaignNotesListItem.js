import React, {Component, PropTypes} from 'react';

class CampaignNotesListItem extends Component {

  static defaultProps = {
    noteContent: ''
  };

  static propTypes = {
    noteContent: PropTypes.string
  };

  render() {
    return (
      <div>
        {this.props.noteContent}
      </div>
    );
  }
}

export default CampaignNotesListItem;
