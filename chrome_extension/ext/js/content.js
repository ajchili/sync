var mediaInterval;

// https://stackoverflow.com/a/46816934
let script = document.createElement('script');
script.text = ['const videoPlayer = netflix.appContext.state.playerApp.getAPI().videoPlayer;',
               'const playerSessionId = videoPlayer.getAllPlayerSessionIds()[0];',
               'const player = videoPlayer.getVideoPlayerBySessionId(playerSessionId);'].join('\n');
document.head.appendChild(script);

chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {
    switch (request.func) {
        case 'setUI':
            setUI(request.user);
            break;
        case 'displayServers':
            displayServers(request.servers);
            break;
        case 'displayServerInfo':
            displayServerInfo(request.server);
            break;
        case 'setMedia':
            if (window.location.pathname.includes('/watch/' + request.media.title)) {
                clearInterval(mediaInterval);

                mediaInterval = setInterval(function() {
                    setMedia(request.media);
                }, 100);
            } else {
                window.location = 'https://www.netflix.com/watch/' + request.media.title;
            }
            break;
        case 'setupMediaListener':
            clearInterval(mediaInterval);

            let video = document.getElementsByTagName('video')[0]
            
            mediaInterval = setInterval(function() {
                sendMessageToBackground({ func: 'updateMedia', media: { paused: video.paused, time: video.currentTime } });
            }, 100);
            break;
        default:
            break;
    }
});

function sendMessageToBackground(message) {
    chrome.runtime.sendMessage(message, null);
}

sendMessageToBackground({ func: 'currentPage', page: window.location });

function setUI(user) {
    if (document.getElementsByClassName('AkiraPlayer')[0] != null
        && !document.getElementsByClassName('AkiraPlayer')[0].classList.value.includes('sync-player')) {
        document.getElementsByClassName('AkiraPlayer')[0].classList.add('sync-player');
    }

    if (!document.getElementById('appMountPoint').classList.contains('sync-netflix')) {
        document.getElementById('appMountPoint').classList.add('sync-netflix');
        document.body.childNodes[1].classList = 'sync';
        document.body.childNodes[1].innerHTML += '<div id="sync"></div>';
    }

    switch (user.state) {
        case 0:
            document.getElementById('sync').innerHTML = '<h3 class="sync-header">sync for Chrome</h3><div class="sync-browser"><ul id="syncServerList"></ul></div><div class="sync-taskbar"><div class="sync-taskbar-button-container"><a id="hostButton" role="link" aria-label="Host"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Host</span></span></a><a role="link" aria-label="Join" hidden><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Join</span></span></a></div></div></div>';

            document.getElementById('hostButton').onclick = function () {
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

            document.getElementById('endButton').onclick = function () {
                sendMessageToBackground({ func: 'endServer' });
            }

            sendMessageToBackground({ func: 'loadServerInfo' });
            break;
        case 2:
            document.getElementById('sync').innerHTML = '<h3 class="sync-header">sync for Chrome</h3><h4 style="margin-left: 10%;">Users</h4><div class="sync-user-list"><ul id="syncUsersList"></ul></div><div class="sync-taskbar"><div class="sync-taskbar-button-container"><a id="disconnectButton" role="link" aria-label="Disconnect"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Disconnect</span></span></a></div></div></div>';

            document.getElementById('disconnectButton').onclick = function () {
                sendMessageToBackground({ func: 'leaveServer' });
            }

            sendMessageToBackground({ func: 'loadServerInfo' });
            break;
    }
}

function displayServers(servers) {
    var serverList = document.getElementById('syncServerList');
    if (serverList != null) {
        serverList.innerHTML = '';

        servers.forEach(function (server) {
            if (server.isPrivate === false) {
                serverList.insertAdjacentHTML('beforeend', '<li class="server"><h4 class="sync-server-title">' + server.title + '<h4><div class="sync-server-button"><a id="join_' + server.key + '" role="link" aria-label="Join"><span tabindex="-1" class="nf-icon-button nf-flat-button nf-flat-button-primary"><span class="nf-flat-button-text">Join</span></span></a></div></li>');

                document.getElementById('join_' + server.key).onclick = function () {
                    sendMessageToBackground({ func: 'joinServer', key: server.key });
                }
            } else {
                serverList.insertAdjacentHTML('beforeend', '<li class="server"><h4 class="sync-server-title">' + server.title + '<h4><div class="sync-server-button"></div></li>');
            }
        });
    }
}

function displayServerInfo(server) {
    var userList = document.getElementById('syncUsersList');
    if (userList != null) {
        userList.innerHTML = '';

        server.users.forEach(function (user) {
            userList.innerHTML += '<li class="user"><h5 class="sync-user-name">' + user.name + '<h5>';
        });
    }
}

function play() {
    let script = document.createElement('script');
    script.text = '(videoPlayer.getVideoPlayerBySessionId(videoPlayer.getAllPlayerSessionIds()[0]).play())();';
    document.head.appendChild(script);
}

function pause() {
    let script = document.createElement('script');
    script.text = '(videoPlayer.getVideoPlayerBySessionId(videoPlayer.getAllPlayerSessionIds()[0]).pause())();';
    document.head.appendChild(script);
}

function seek(place) {
    let script = document.createElement('script');
    script.text = '(videoPlayer.getVideoPlayerBySessionId(videoPlayer.getAllPlayerSessionIds()[0]).seek(' + place + '))();';
    document.head.appendChild(script);
}

function setMedia(media) {
    let video = document.getElementsByTagName('video')[0];

    if (video != null) {
        if (video.paused != media.paused) {
            if (media.paused) {
                pause();
            } else {
                play();
            }
        }
    
        if ((video.currentTime < media.time - 1 || video.currentTime > media.time + 1) && video.buffered.length > 0) {
            seek(media.time);
        }
    }
}