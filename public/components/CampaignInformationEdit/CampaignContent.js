import React, {Component, PropTypes} from 'react';
import ProgressSpinner from '../utils/ProgressSpinner';

class CampaignContent extends Component {

  componentWillMount() {
    this.props.campaignContentActions.getCampaignContent(this.props.campaign.id);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.campaign.id !== this.props.campaign.id) {
      this.props.campaignContentActions.getCampaignContent(nextProps.campaign.id);
    }
  }
  renderContentAtoms = (atom) => {
    return (
      <div key={atom.id} className="campaign-content-list__row">
        <div className="campaign-content-list__content-type"></div>
        <div className="campaign-content-list__content-title">{atom.type}: {atom.title}</div>
        <div className="campaign-content-list__content-status"></div>
        <div className="campaign-content-list__content-links">Links</div>
      </div>
    );
  }

  renderContentItem = (content) => {
    return (
      <span>
        <div key={content.id} className="campaign-content-list__row">
          <div className="campaign-content-list__content-type">{content.type}</div>
          <div className="campaign-content-list__content-title">{content.title}</div>
          <div className="campaign-content-list__content-status">Status</div>
          <div className="campaign-content-list__content-links">Links</div>
        </div>
        {content.atoms.map( this.renderContentAtoms )}
      </span>
    );
  }

  renderContentItems = () => {

    if(!this.props.campaignContent) {
      return (<ProgressSpinner />);
    }

    console.log('content', this.props.campaignContent, this.props.campaignContent.length);

    if(this.props.campaignContent.length > 0) {
      return (
        <div className="campaign-content-list">
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
      <div className="campaign-assets campaign-box-section">
        <div className="campaign-box-section__header">
          Campaign Content
        </div>
        <div className="campaign-box-section__body">
          <div className="campaign-assets__field">
            <label>Content</label>
            {this.renderContentItems()}
          </div>
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaignContent from '../../actions/CampaignActions/getCampaignContent';

function mapStateToProps(state) {
  return {
    campaignContent: state.campaignContent,
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignContentActions: bindActionCreators(Object.assign({}, getCampaignContent), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignContent);
