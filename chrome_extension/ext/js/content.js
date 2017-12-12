var url = window.location.href;
var state = 0;
var key = '';

if (url.includes("browse")) {
    loadBrowseElements();
}

function loadBrowseElements() {
    document.body.innerHTML = '<div class="sync-netflix">' + document.body.innerHTML + '</div>';
    document.body.innerHTML += '<div class="sync"/><h3 class="sync-header">sync for Chrome</h3><div class="sync-browser"><ul id="syncServerList"></ul></div><div class="sync-taskbar"><div class="sync-taskbar-button-container"><a role="link" aria-label="Host"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Host</span></span></a><a role="link" aria-label="Join"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Join</span></span></a></div></div></div>';
    
    loadServers();
}

function loadServers() {
    document.getElementById('syncServerList').innerHTML = ''
}

function askIfShouldShowServers() {
    
}

function createServer() {
    var serverKey = firebase.database().ref().child('servers').push().key;
    state = 1;
    key = serverKey;
}

function joinServer(key) {
    
}