import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Button from 'material-ui/Button';
import Card, { CardActions, CardContent } from 'material-ui/Card';
import { MuiThemeProvider } from 'material-ui/styles';
import TextField from 'material-ui/TextField';

class SignUp extends Component {

  render() {
    const { swipeToSignIn } = this.props;

    return (
      <Card
        style={{
          width: '50%',
          marginLeft: '25%',
          textAlign: 'center',
        }}>
        <CardContent>
          <h1>Create Account</h1>
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
          <TextField
            id="verifyPassword"
            label="Confirm Password"
            type="password"
            margin="normal"
          />
          <br />
          <div
            style={{
              marginTop: '1em',
            }}>
            <Button
              variant="raised"
              color="primary">
              Create Account
            </Button>
          </div>
          <br />
          <Button
            onClick={swipeToSignIn}
            color="primary"
            style={{
              marginTop: '1em',
            }}>
            Back
          </Button>
        </CardContent>
      </Card>
    )
  };
}

SignUp.propTypes = {
  swipeToSignIn: PropTypes.func,
};

export default SignUp;
