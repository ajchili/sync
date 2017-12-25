chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
    switch(request.func) {
        case 'setUI':
            setUI(request.user);
            break;
        case 'displayServers':
            break;
        default:
            break;
    }
});

function sendMessageToBackground(message) {
    chrome.runtime.sendMessage(message, function(response) {});
}

function setUI(user) {
    document.getElementsByClassName('ArkiaPlayer').classList += 'sync-player';
    document.getElementById('appMountPoint').classList += 'sync-netflix';
    document.body.childNodes[1].classList = 'sync';
    document.body.childNodes[1].innerHTML += '<h3 class="sync-header">sync for Chrome</h3><div class="sync-browser"><ul id="syncServerList"></ul></div><div class="sync-taskbar"><div class="sync-taskbar-button-container"><a role="link" aria-label="Host"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Host</span></span></a><a role="link" aria-label="Join"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Join</span></span></a></div></div></div>';
}

function displayServers(servers) {
    var serverList = document.getElementById('syncServerList');
    serverList.innerHTML = '';

    servers.forEach(function(server) {
        serverList.innerHTML += '<li class="server"><h4 class="sync-server-title">' + server.title + '<h4><div class="sync-server-button"><a role="link" aria-label="Join" onclick="";><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Join</span></span></a></div></li>';
    });
}