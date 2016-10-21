import React, {Component, PropTypes} from 'react';
import ProgressSpinner from '../utils/ProgressSpinner';
import {composerEditUrl, previewUrl, liveUrl, mediaAtomEditUrl} from '../../util/urlBuilder';

class CampaignContent extends Component {

  componentWillMount() {
    this.props.getCampaignContent(this.props.campaign.id);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.campaign.id !== this.props.campaign.id) {
      this.props.getCampaignContent(nextProps.campaign.id);
    }
  }

  renderContentAtoms = (atom) => {
    return (
      <div key={atom.id} className="campaign-content-list__row">
        <div className="campaign-content-list__content-type"></div>
        <div className="campaign-content-list__atom-title">{atom.type}: {atom.title}</div>
        <div className="campaign-content-list__content-status"></div>
        <div className="campaign-content-list__content-links">
          <a href={mediaAtomEditUrl(atom.id)} target="_blank" title="Edit in atom builder"><i className="i-atom" /></a>
        </div>
      </div>
    );
  }

  renderContentItem = (content) => {
    var contentTypeIcon;

    if (content.type === 'Article') {
      contentTypeIcon = <i className="i-article" />
    } else if (content.type === 'Gallery') {
      contentTypeIcon = <i className="i-gallery" />
    } else if (content.type === 'Video') {
      contentTypeIcon = <i className="i-video" />
    }

    var status;
    if(content.isLive) {
      status = 'Live';
    } else {
      status = 'Draft';
    }

    return (
      <div key={content.id} className="campaign-content-list__item">
        <div className="campaign-content-list__row">
          <div className="campaign-content-list__content-type">{contentTypeIcon}{content.type}</div>
          <div className="campaign-content-list__content-title">{content.title}</div>
          <div className="campaign-content-list__content-status">{status}</div>
          <div className="campaign-content-list__content-links">
            <a href={composerEditUrl(content.composerId)} target="_blank" title="Edit in composer"><i className="i-composer" /></a>
            <a href={previewUrl(content.path)} target="_blank" title="Preview"><i className="i-preview-eye" /></a>
            <a href={liveUrl(content.path)} target="_blank" title="See live"><i className="i-live-site" /></a>
          </div>
        </div>
        {content.atoms.map( this.renderContentAtoms )}
      </div>
    );
  }

  renderContentItems = () => {

    if(!this.props.campaignContent) {
      return (<ProgressSpinner />);
    }

    if(this.props.campaignContent.length > 0) {
      return (
        <div className="campaign-content-list campaign-assets__field__value">
          <div className="campaign-content-list__row">
            <div className="campaign-content-list__content-type--header">Type</div>
            <div className="campaign-content-list__content-title--header">Title</div>
            <div className="campaign-content-list__content-status--header">Status</div>
            <div className="campaign-content-list__content-links--header">Links</div>
          </div>
          {this.props.campaignContent.map( this.renderContentItem ) }
        </div>
      );
    }

    return (
      <span className="campaign-assets__field__value">
        No content has been created yet
      </span>
    )
  }

  render() {

    return (
      <div className="campaign-assets">
        <div className="campaign-assets__field">
          <label>Content</label>
          {this.renderContentItems()}
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';

function mapStateToProps(state) {
  return {
    campaignContent: state.campaignContent
  };
}


export default connect(mapStateToProps)(CampaignContent);
