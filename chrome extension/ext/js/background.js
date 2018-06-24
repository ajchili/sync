var ref = firebase.database().ref().child('chrome');
var user = { uid: checkForUser() };
var userListener;
var mediaListener;

chrome.tabs.onUpdated.addListener(function (tabID, changeInfo, tab) {
    if (tab.url && tab.url.indexOf('https://www.netflix.com/') == 0) {
        setupUser();

        if (user.state === 1) {
            if (tab.url.includes('/watch/')) {
                setServerMedia(tab.url);
                sendMessageToContent({ func: 'setupMediaListener' })
            } else {
                clearServerMedia();
            }
        }
    }
});

chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {
    switch (request.func) {
        case 'setUserName':
            setUserName(request.name);
            break;
        case 'setUserState':
            setUserName(request.state);
            break;
        case 'loadServers':
            loadServers();
            break;
        case 'createServer':
            createServer(request.title);
            break;
        case 'joinServer':
            joinServer(request.key);
            break;
        case 'endServer':
            endServer();
            break;
        case 'leaveServer':
            leaveServer();
            break;
        case 'loadServerInfo':
            loadServerInfo();
            break;
        case 'currentPage':
            handleCurrentPage(request.page);
            break;
        case 'updateMedia':
            if (user.state === 1) {
                updateMedia(request.media);
            }
            break;
        case 'sendMessage':
            if (user.state === 1) {
                senMessage(request.message);
            }
            break;
        default:
            break;
    }
});

function handleCurrentPage(window) {
    if (user.state === 1) {

    } else if (user.state === 2) {
        if (user.server != null) {
            setMediaListener();
        }
    }
}

function sendMessageToContent(message) {
    chrome.tabs.query({ active: true, currentWindow: true }, function (tabs) {
        chrome.tabs.sendMessage(tabs[0].id, message, null);
    });
}

function checkForUser() {
    if (document.cookie.includes('syncUID=')) {
        var cookie = document.cookie;
        return cookie.substring(cookie.indexOf('syncUID=') + 8);
    } else {
        var uid = ref.child('users').push().key;
        document.cookie = `syncUID=${uid};`;
        return uid;
    }
}

function setupUser() {
    ref.child(`users/${user.uid}`).once('value').then(function (snapshot) {
        if (!snapshot.exists()) {
            ref.child(`users/${user.uid}`).set({ state: 0, name: 'syncer' });
        } else if (!snapshot.child('name').exists()) {
            ref.child(`users/${user.uid}/name`).set('syncer');
        }
    });

    if (userListener) {
        userListener.off();
    }

    userListener = ref.child('users').child(user.uid);
    userListener.on('value', function (snapshot) {
        user.name = snapshot.child('name').val();
        user.state = snapshot.child('state').val();
        user.server = snapshot.child('server').val();

        setUI();
    });
}

function setUserName(name) {
    ref.child(`users/${user.uid}/name`).set(name);
}

function setUserState(state) {
    ref.child(`users/${user.uid}/state`).set(state);
}

function setMediaListener() {
    mediaListener = ref.child(`servers/${user.server}/media`);
    mediaListener.on('value', function (media) {
        if (media.exists()) {
            sendMessageToContent({ func: 'setMedia', media: media });
        }
    });
}

function createServer(title) {
    var key = ref.child('servers').push().key;
    ref.child(`servers/${key}`).set({
        title: title,
        isPrivate: false,
        host: user.uid,
        users: {
            host: user.uid
        }
    });
    ref.child(`users/${user.uid}/server`).set(key);
    setUserState(1);
}

function joinServer(serverKey) {
    var key = ref.child(`servers/${serverKey}/users`).push().key;
    ref.child(`servers/${serverKey}/users/${key}`).set(user.uid);
    ref.child(`users/${user.uid}/server`).set(serverKey);
    setUserState(2);
    setMediaListener();
}

function setServerMedia(url) {
    let isTracked = url.includes('?trackId');
    let media = null;

    if (isTracked) {
        media = url.substring(url.lastIndexOf('/') + 1, url.indexOf('?'));
    } else {
        media = url.substring(url.lastIndexOf('/') + 1);
    }

    ref.child(`servers/${user.server}/media`).set({
        title: media,
        paused: true,
        time: 0
    });
}

function updateMedia(media) {
    ref.child(`servers/${user.server}/media`).update({
        paused: media.paused,
        time: media.time
    });
}

function sendMessage(message) {
    var key = ref.child(`servers/${user.server}/messages/`).push().key;
    ref.child(`servers/${user.server}/messages/${key}`).set({
        sender: user.uid,
        message: message
    });
}

function clearServerMedia() {
    if (user.server != null) {
        ref.child(`servers/${user.server}/media`).remove();
    }
}

function endServer() {
    ref.child(`servers/${user.server}`).once('value').then(function (snapshot) {
        if (snapshot.exists()) {
            var serverId = user.server;

            if (user.uid === snapshot.child('host').val()) {
                snapshot.child('users').forEach(function (childSnapshot) {
                    ref.child(`users/${childSnapshot.val()}/state`).set(0);
                    ref.child(`users/${childSnapshot.val()}/server`).remove();
                });
            }

            ref.child(`servers/${serverId}`).remove();
        }
    });
}

function leaveServer() {
    if (mediaListener) {
        mediaListener.off();
    }

    ref.child(`servers/${user.server}/users`).once('value').then(function (snapshot) {
        snapshot.forEach(function (childSnapshot) {
            if (user.uid === childSnapshot.val()) {
                ref.child(`servers/${user.server}/users`).child(childSnapshot.key).remove();
                ref.child(`users/${user.uid}/state`).set(0);
                ref.child(`users/${user.uid}/server`).remove();
            }
        });
    });
}

function setUI() {
    sendMessageToContent({ func: 'setUI', user: user });
}

function loadServers() {
    ref.child('servers').on('value', function (snapshot) {
        var servers = [];

        snapshot.forEach(function (childSnapshot) {
            var key = childSnapshot.key;
            var title = childSnapshot.child('title').val();
            var isPrivate = childSnapshot.child('isPrivate').val();

            servers.push({ key: key, title: title, isPrivate: isPrivate });
        });

        sendMessageToContent({ func: 'displayServers', servers: servers });
    });
}

function loadServerInfo() {
    ref.child(`servers/${user.server}`).on('value', function (snapshot) {
        var server = [];
        var users = [];

        snapshot.child('users').forEach(function (childSnapshot) {
            ref.child(`users/${childSnapshot.val()}`).once('value').then(function (childChildSnapshot) {
                users.push({ id: childSnapshot.val(), name: childChildSnapshot.child('name').val() });
            });
        });

        server = { users: users };
        sendMessageToContent({ func: 'displayServerInfo', server: server });
    });
}
