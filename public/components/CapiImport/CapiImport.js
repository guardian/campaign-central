import React, {Component, PropTypes} from 'react';
import TagPicker from '../utils/TagPicker';
import ProgressSpinner from '../utils/ProgressSpinner'
import {importCampaignFromTag} from '../../services/CampaignsApi';

class CapiImport extends Component {

  static contextTypes = {
    router: React.PropTypes.object.isRequired
  }

  constructor(props) {
    super(props);
    this.state = {
      importing: false
    };
  };

  tagSelected = (tag) => {
    this.setState({importing: true});
    importCampaignFromTag(tag).then((resp) => {
      this.setState({importing: false});
      this.context.router.push('/campaign/' + resp.id);
    })
  }

  renderThobber = () => {
    if(this.state.importing) {
      return(<ProgressSpinner />);
    }
    return;
  }

  render() {

    return (
      <div className="campaigns">
        <h2 className="campaigns__header">Campaign importer</h2>
        <p>Select a campaign tag (hosted or paid content) and we'll import it as a campaign.</p>
        <TagPicker type="paidContent" onTagSelected={this.tagSelected}/>
        {this.renderThobber()}
      </div>
    );
  }
}

export default CapiImport;
