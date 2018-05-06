const electron = require('electron');
const express = require('express');
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
    minHeight: 768,
    frame: process.platform !== 'darwin',
    titleBarStyle: process.platform !== 'darwin' ? 'default' : 'hidden',
    show: false
  });

  mainWindow.loadURL(url.format({
    pathname: path.join(__dirname, 'public/index.html'),
    protocol: 'file:',
    slashes: true
  }));

  mainWindow.on('closed', function () {
    mainWindow = null;
  });

  mainWindow.once('ready-to-show', function () {
    mainWindow.show()
  });

  mainWindow.webContents.on('new-window', function (e, url) {
    e.preventDefault();
    electron.shell.openExternal(url);
  });
};

app.on('ready', createWindow);
expressApp.use('/media', express.static(__dirname + '/media'));
expressApp.use('/user', require('./routes/user'));
expressApp.listen(3000);

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
