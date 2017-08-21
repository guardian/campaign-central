import React, {Component, PropTypes} from 'react';
import ProgressSpinner from '../utils/ProgressSpinner';
import {searchTags} from '../../services/TagManagerApi';
import {importCampaignFromTag} from '../../services/CampaignsApi';

class CapiImport extends Component {

  static contextTypes = {
    router: React.PropTypes.object.isRequired
  }

  constructor(props) {
    super(props);
    this.state = {
      error: '',
      selectedTag: undefined,
      tagSearchTerm: '',
      campaignValue: undefined,
      uniquesTarget: undefined,
      pageviewsTarget: undefined,
      suggestions: [],
      importing: false
    };
  };

  updateSearchField = (e) => {
    const searchTerm = e.target.value;
    this.setState({
      tagSearchTerm: searchTerm
    });

    if (searchTerm.length !== 0) {
      this.performSearch(searchTerm);
    } else {
      this.setState({
        suggestions: []
      });
    }
  };

  onClickTag(tag) {
    this.setState({
      tagSearchTerm: tag.externalName,
      selectedTag: tag,
      suggestions: []
    });
  };

  onCapiImportSubmit = (e) => {
    e.preventDefault();

    const isCampaignValueSet = this.state.campaignValue && this.state.campaignValue > 0;
    const isUniquesTargetSet = this.state.uniquesTarget && this.state.uniquesTarget > 0;
    const isPageviewsTargetSet = this.state.pageviewsTarget && this.state.pageviewsTarget > 0;
    const selectedTag = this.state.selectedTag;


    if (isCampaignValueSet && isUniquesTargetSet && selectedTag) {
      this.setState({importing: true});

      importCampaignFromTag(selectedTag).then((campaign) => {

        let updatedTargets = undefined;

        if (isPageviewsTargetSet) {
          updatedTargets = Object.assign({}, campaign.targets, { ['uniques']: this.state.uniquesTarget, ['pageviews']: this.state.pageviewsTarget });
        } else {
          updatedTargets = Object.assign({}, campaign.targets, { ['uniques']: this.state.uniquesTarget });
        }

        this.props.updateCampaign(Object.assign({}, campaign, {
          actualValue: this.state.campaignValue,
          targets: updatedTargets
        }));

        this.setState({importing: false});
        this.context.router.push('/campaign/' + campaign.id);
      })

    } else {
      this.setState({
        error: 'All required fields must be present before the campaign can be imported.'
      });
    }
  }

  validateNumericInput = (value, errorState, successState) => {
    const numValue = parseInt(value);

    if (!value) {
      this.setState({error: ''});
    } else if (isNaN(numValue) || numValue != value) {
      this.setState(errorState);
    } else {
      this.setState(successState);
    }

  };

  onCampaignValueChange = (e) => {
    const campaignValue = e.target.value;
    const successState = { error: '', campaignValue: campaignValue };
    const errorState = { error: 'Campaign value has to be a number!' };

    this.validateNumericInput(campaignValue, errorState, successState);
  };

  onPageviewsTargetChange = (e) => {
    const pageviewsTarget = e.target.value;
    const successState = { error: '', pageviewsTarget: pageviewsTarget };
    const errorState = { error: 'Pageviews target value has to be a number!' };

    this.validateNumericInput(pageviewsTarget, errorState, successState);
  };

  onUniquesTargetChange = (e) => {
    const uniquesTarget = e.target.value;
    const successState = { error: '', uniquesTarget: uniquesTarget };
    const errorState = { error: 'Uniques target value has to be a number!' };

    this.validateNumericInput(uniquesTarget, errorState, successState);
  };

  performSearch(searchTerm) {
    const searchParams = {query: searchTerm || this.state.searchTerm};
    if (this.props.type) {searchParams.type = this.props.type};
    if (this.props.subtype) {searchParams.subType = this.props.subtype};

    searchTags(searchParams)
      .then((tags) => {
        this.setState({
          suggestions: tags.data.map(t => {
            let data = t.data;
            data.uri = t.uri;
            return data;
          })
        });
      })
  };

  renderSuggestions = () => {
    return (
      <ul>
        {this.state.suggestions.map((tag) =>
          <li key={tag.id} onClick={() => this.onClickTag(tag)}>
            <div className="tag-select__name">{tag.internalName}</div>
          </li>
        )}
      </ul>
    )
  };

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
        <form className="pure-form pure-form-aligned">
          <fieldset>
            <div className="pure-control-group">
              <label htmlFor="name">Campaign Value (£)</label>
              <input id="name" type="text" placeholder="" onChange={this.onCampaignValueChange} />
                <span className="pure-form-message-inline">required</span>
            </div>

            <div className="pure-control-group">
              <label htmlFor="name">Uniques Target</label>
              <input id="name" type="text" placeholder="" onChange={this.onUniquesTargetChange} />
              <span className="pure-form-message-inline">required</span>
            </div>

            <div className="pure-control-group">
              <label htmlFor="name">Pageviews Target</label>
              <input id="name" type="text" placeholder="" onChange={this.onPageviewsTargetChange}/>
              <span className="pure-form-message-inline">optional</span>
            </div>

            <div className="pure-control-group">
              <label htmlFor="name">Pick Tag</label>
              <input
                id="input-tag-picker"
                type="text" autoFocus={true}
                value={this.state.tagSearchTerm}
                onChange={this.updateSearchField}
                placeholder="Start typing ..."/>
              <span className="pure-form-message-inline">required</span>

              <div className="tag-select__suggestions">
                {this.renderSuggestions()}
              </div>
            </div>

            <span className="campaign-info__field__error">{this.state.error}</span>

            <div className="pure-controls">
              <button type="submit" className="pure-button pure-button-primary" onClick={this.onCapiImportSubmit}>Submit</button>
            </div>
          </fieldset>
        </form>



        {this.renderThobber()}
      </div>
    );
  }
}

export default CapiImport;
