import React, { Component } from 'react';
import Button from 'material-ui/Button';
import Card, { CardActions, CardContent } from 'material-ui/Card';
import { MuiThemeProvider } from 'material-ui/styles';
import TextField from 'material-ui/TextField';
import Theme from '../utils/Theme';

export default class SignIn extends Component {

  render() {
    return (
      <MuiThemeProvider theme={Theme}>
        <div
          style={{
            width: '100%',
            height: '100%',
            backgroundColor: Theme.palette.primary.main,
          }}>
          <div
            style={{
              width: '100%',
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'center',
            }}>
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
                  color="primary"
                  style={{
                    marginTop: '1em',
                  }}>
                  Forgot Password
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </MuiThemeProvider>
    )
  };
}
