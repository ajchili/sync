import React, { Component } from 'react';
import SwipeableViews from 'react-swipeable-views';
import AppBar from 'material-ui/AppBar';
import Tabs, { Tab } from 'material-ui/Tabs';
import { MuiThemeProvider } from 'material-ui/styles';
import Theme from '../utils/Theme';
import SignUp from './SignUp';
import SignIn from './SignIn';

export default class Initial extends Component {
  state = {
    value: 1,
  };

  handleChange = (event, value) => {
    this.setState({ value });
  };

  handleChangeIndex = index => {
    this.setState({ value: index });
  };

  swipeToSignIn = () => {
    this.handleChange(null, 1);
  };

  swipeToSignUp = () => {
    this.handleChange(null, 0);
  };

  swipeToForgotPassword = () => {
    this.handleChange(null, 2);
  };

  render() {
    return (
      <MuiThemeProvider theme={Theme}>
        <div
          style={{
            width: '100%',
            height: '100%',
            backgroundColor: Theme.palette.primary.main,
          }}>
          <SwipeableViews
            index={this.state.value}
            onChangeIndex={this.handleChangeIndex}
            style={{
              width: '100%',
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'center',
            }}>
            <SignUp
              swipeToSignIn={this.swipeToSignIn} />
            <SignIn
              swipeToSignUp={this.swipeToSignUp}
              swipeToForgotPassword={this.swipeToForgotPassword}/>
            <div />
          </SwipeableViews>
        </div>
      </MuiThemeProvider>
    );
  }
}
