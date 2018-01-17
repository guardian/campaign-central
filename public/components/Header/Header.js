import React from 'react';
import { Link } from 'react-router-dom';

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
                  This tool is under active development and we want your feedback to make it better. Contact the team on <span style={{color: '#37c7ba'}}><a href="mailto:commercial.dev@theguardian.com?subject=Campaign%20Central%20Feedback">commercial.dev@theguardian.com</a></span>
                </div>
            </header>
        );
    }
}
