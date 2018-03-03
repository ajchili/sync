import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Button from 'material-ui/Button';
import Card, { CardActions, CardContent } from 'material-ui/Card';
import { MuiThemeProvider } from 'material-ui/styles';
import TextField from 'material-ui/TextField';

class SignIn extends Component {

  render() {
    const { swipeToSignUp, swipeToForgotPassword } = this.props;

    return (
      <Card
        style={{
          width: '50%',
          marginLeft: '25%',
          textAlign: 'center',
        }}>
        <CardContent>
          <h1>Login</h1>
          <TextField
            id="email"
            label="Email Address"
            type="email"
            margin="normal"
          />
          <br />
          <TextField
            id="password"
            label="Password"
            type="password"
            margin="normal"
          />
          <br />
          <div
            style={{
              marginTop: '1em',
            }}>
            <Button
              onClick={swipeToSignUp}
              variant="raised"
              color="primary">
              Create Account
            </Button>
            <Button
              variant="raised"
              color="primary"
              style={{
                marginLeft: '1em',
              }}>
              Login
            </Button>
          </div>
          <br />
          <Button
            onClick={swipeToForgotPassword}
            color="primary"
            style={{
              marginTop: '1em',
            }}>
            Forgot Password
          </Button>
        </CardContent>
      </Card>
    )
  };
}

SignIn.propTypes = {
  swipeToSignUp: PropTypes.func,
  swipeToForgotPassword: PropTypes.func,
};

export default SignIn;
