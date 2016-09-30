import React, {Component, PropTypes} from 'react';
import TagPicker from '../utils/TagPicker';
import {importCampaignFromTag} from '../../services/CampaignsApi';

class CapiImport extends Component {


  tagSelected = (tag) => {
    console.log('tag picked', tag);
    importCampaignFromTag(tag).then((resp) => {
      console.log('imported tag, resp', resp);
    })
    
  }

  render() {

    return (
      <div className="campaigns">
        <h2 className="campaigns__header">Capi importer</h2>
        <p>Select a hosted content camapign tag and we'll import it as a campaign.</p>
        <TagPicker type="paidContent" subtype="HostedContent" onTagSelected={this.tagSelected}/>
      </div>
    );
  }
}

export default CapiImport;