const electron = require('electron');
const express = require('express');
const localtunnel = require('localtunnel');
const app = electron.app;
const expressApp = express();
const BrowserWindow = electron.BrowserWindow;

const path = require('path');
const url = require('url');

let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1024,
    height: 768,
    minWidth: 1024,
    minHeight: 768
  });

  mainWindow.loadURL(url.format({
    pathname: path.join(__dirname, 'public/index.html'),
    protocol: 'file:',
    slashes: true
  }));

  mainWindow.on('closed', function () {
    mainWindow = null;
  });
};

app.on('ready', createWindow);
expressApp.use('/media', express.static('media'));
expressApp.listen(3000);
let tunnel = localtunnel(3000, function(err, tunnel) {
  console.log(tunnel.url);
}); 

app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', function () {
  if (mainWindow === null) {
    createWindow();
  }
});
