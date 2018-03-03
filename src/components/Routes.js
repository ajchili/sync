import React, { Component } from 'react';
import { render } from 'react-dom';
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Redirect
} from 'react-router-dom';
import onAuth from './helpers/auth';
import SignIn from './pages/SignIn';

export default class Routes extends Component {

  constructor(props) {
    super(props);
    this.state = {
      authed: false,
    }
  }

  componentDidMount () {
    this.removeListener = onAuth((user) => {
      this.setState({
        authed: user != null,
      });
    });
  }

  componentWillUnmount () {
    this.removeListener();
  }

  render() {
    return(
      <Router>
        <Switch>
          <Route component={SignIn}/>
        </Switch>
      </Router>
    );
  }
}
