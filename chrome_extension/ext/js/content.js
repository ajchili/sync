chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
    switch(request.func) {
        case 'setUI':
            setUI(request.user);
            break;
        case 'displayServers':
            displayServers(request.servers);
            break;
        case 'displayServerInfo':
            displayServerInfo(request.server);
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
    
    if (!document.getElementById('appMountPoint').classList.contains('sync-netflix')) {
        document.getElementById('appMountPoint').classList += 'sync-netflix';
        document.body.childNodes[1].classList = 'sync';
        document.body.childNodes[1].innerHTML += '<div id="sync"></div>';
    }

    switch(user.state) {
        case 0:
            document.getElementById('sync').innerHTML = '<h3 class="sync-header">sync for Chrome</h3><div class="sync-browser"><ul id="syncServerList"></ul></div><div class="sync-taskbar"><div class="sync-taskbar-button-container"><a id="hostButton" role="link" aria-label="Host"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Host</span></span></a><a role="link" aria-label="Join"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Join</span></span></a></div></div></div>';
    
            document.getElementById('hostButton').onclick = function() {
                var title = prompt('Please enter a tile', '');
                if (title.length > 0) {
                    sendMessageToBackground({ func: 'createServer', title: title });
                } else {
                    alert('Server must have title!');
                }
            }

            sendMessageToBackground({ func: 'loadServers' });

            break;
        case 1:
            document.getElementById('sync').innerHTML = '<h3 class="sync-header">sync for Chrome</h3><h4 style="margin-left: 10%;">Users</h4><div class="sync-user-list"><ul id="syncUsersList"></ul></div><div class="sync-taskbar"><div class="sync-taskbar-button-container"><a id="endButton" role="link" aria-label="End"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">End</span></span></a></div></div></div>';
    
            document.getElementById('endButton').onclick = function() {
                sendMessageToBackground({ func: 'endServer' });
            }

            sendMessageToBackground({ func: 'loadServerInfo' });
            break;
        case 2:
            document.getElementById('sync').innerHTML = '<h3 class="sync-header">sync for Chrome</h3><h4 style="margin-left: 10%;">Users</h4><div class="sync-user-list"><ul id="syncUsersList"></ul></div><div class="sync-taskbar"><div class="sync-taskbar-button-container"><a id="disconnectButton" role="link" aria-label="Disconnect"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Disconnect</span></span></a></div></div></div>';
    
            document.getElementById('disconnectButton').onclick = function() {
                sendMessageToBackground({ func: 'leaveServer' });
            }

            sendMessageToBackground({ func: 'loadServerInfo' });
            break;
    }
}

function displayServers(servers) {
    var serverList = document.getElementById('syncServerList');
    serverList.innerHTML = '';

    servers.forEach(function(server) {
        if (!server.isPrivate) {
            serverList.innerHTML += '<li class="server"><h4 class="sync-server-title">' + server.title + '<h4><div class="sync-server-button"><a id="join_' + server.key +'" role="link" aria-label="Join"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Join</span></span></a></div></li>';
    
            document.getElementById('join_' + server.key).onclick = function() {
                sendMessageToBackground({ func: 'joinServer', key: server.key });
            }
        }
    });
}

function displayServerInfo(server) {
    var userList = document.getElementById('syncUsersList');
    userList.innerHTML = '';

    server.users.forEach(function(user) {
        userList.innerHTML += '<li class="user"><h5 class="sync-user-name">' + user.name + '<h5>';
    });
}