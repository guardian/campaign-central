import React from 'react';
import { Link } from 'react-router';

export default class Header extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <header className="header">
                <Link to="/" className="header__logo">
                    <div className="header__logo__text--large">Campaign</div>
                    <div className="header__logo__text--small">Central</div>
                </Link>
                <div className="header__title">
                  Note this tool is currently in beta and under active development and we want your feedback to make it better.
                  Please contact the team on <span style={{color: '#37c7ba'}}>commercial.dev@guardian.co.uk</span>
                </div>
            </header>
        );
    }
}
