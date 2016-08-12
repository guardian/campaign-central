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
                    Manage your campaigns
                </div>
            </header>
        );
    }
}
