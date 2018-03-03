import React, { Component } from 'react';
import { render } from 'react-dom';
import Routes from './components/Routes';

const mainDivStyle = {
  width: '100%',
  height: '100vh',
};

render((
  <div style={mainDivStyle}>
    <Routes />
  </div>
), document.getElementById('app'));
