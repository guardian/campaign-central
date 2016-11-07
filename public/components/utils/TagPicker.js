import React, { PropTypes } from 'react';
import {searchTags} from '../../services/TagManagerApi';

class TagPicker extends React.Component {

  static propTypes = {
    type: PropTypes.string,
    subtype: PropTypes.string,
    onTagSelected: PropTypes.func
  };

  static defaultProps = {
    value: ""
  }

  constructor(props) {
    super(props);
    this.state = {
      selectedTag: undefined,
      tagSearchTerm: '',
      suggestions: []
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

  onClickTag(tag) {
    this.props.onTagSelected(tag);
    this.setState({
      selectedTag: undefined,
      tagSearchTerm: '',
      suggestions: []
    });
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

  render() {
    return (
      <div className="tag-select">
        <div>
          <input
            className="tag-select__input"
            type="text" autoFocus={true}
            value={this.state.tagSearchTerm}
            onChange={this.updateSearchField}/>
        </div>
        <div className="tag-select__suggestions">
            {this.renderSuggestions()}
        </div>
      </div>
    );
  }
}

export default TagPicker;

