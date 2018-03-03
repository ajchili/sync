import React, { Component } from 'react';
import Button from 'material-ui/Button';

export default class LaunchView extends Component {

  render() {
    return (
      <div
        style={{
          width: '100%',
          height: '100%',
        }}>
        <div
          style={{
            width: '100%',
            top: '5%',
            position: 'absolute',
            textAlign: 'center',
          }}>
          <h1>sync</h1>
        </div>
        <div
          style={{
            width: '100%',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
          }}>
          <div
            style={{
              textAlign: 'center',
            }}>
            <Button variant="raised">
              Host
            </Button>
            <Button
              style={{
                marginLeft: '5%',
              }}
              variant="raised">
              Join
            </Button>
          </div>
        </div>
      </div>
    )
  }
};
