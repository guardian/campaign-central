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

    const payload = {
      tag: this.state.selectedTag,
      campaignValue: this.state.campaignValue,
      uniquesTarget: this.state.uniquesTarget,
      pageviewTarget: this.state.pageviewsTarget
    };

    if (isCampaignValueSet && isUniquesTargetSet && selectedTag) {
      this.setState({importing: true});

      importCampaignFromTag(payload).then((campaign) => {
        this.setState({importing: false});
        this.context.router.push('/campaign/' + campaign.id);
      }).catch((error) => {
        console.error(error);
        this.setState({importing: false, error: 'Something went wrong; please try again'});
      });

    } else {
      this.setState({
        error: 'All required fields must be present before the campaign can be imported.'
      });
    }
  };

  validateState = () => {
    const errors = ['campaignValue', 'uniquesTarget', 'pageviewsTarget'].reduce((errorsAccum, dataToCheck) => {
      const value = this.state[dataToCheck];
      if (value !== undefined && isNaN(value)) {
        errorsAccum.push(`${dataToCheck} value has to be a number!`);
      }

      return errorsAccum;
    }, []);

    if (errors.length > 0) {
      this.setState({error: errors[0]});
    } else {
      this.setState({error: ''});
    }
  };

  onInputChange = (keyName, event) => {
    const newValue = event.target.value;

    if (newValue === '' || newValue === undefined) {
      this.setState({[keyName]: undefined}, this.validateState);
    } else if (newValue) {
      const valueAsInt = Number(newValue);
      this.setState({[keyName]: valueAsInt}, this.validateState);
    }
  };


  performSearch(searchTerm) {
    const searchParams = {query: searchTerm || this.state.searchTerm};
    searchParams.type = 'paidContent';

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
  };

  render() {

    return (
      <div className="campaigns">
        <h2 className="campaigns__header">Campaign importer</h2>
        <form className="pure-form pure-form-aligned">
          <fieldset>
            <div className="pure-control-group">
              <label htmlFor="name">Campaign Value (£)</label>
              <input id="name" type="text" placeholder="" onChange={this.onInputChange.bind(this, 'campaignValue')} />
                <span className="pure-form-message-inline">required</span>
            </div>

            <div className="pure-control-group">
              <label htmlFor="name">Uniques Target</label>
              <input id="name" type="text" placeholder="" onChange={this.onInputChange.bind(this, 'uniquesTarget')} />
              <span className="pure-form-message-inline">required</span>
            </div>

            <div className="pure-control-group">
              <label htmlFor="name">Pageviews Target</label>
              <input id="name" type="text" placeholder="" onChange={this.onInputChange.bind(this, 'pageviewsTarget')}/>
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
