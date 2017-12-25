var ref = firebase.database().ref();
var user = { uid: checkForUser() };
setupUser();

chrome.tabs.onUpdated.addListener(function(tabID, changeInfo, tab) {
    if (tab.url) {
        if (tab.url.indexOf('https://www.netflix.com/') == 0) {
            setUI();
        }
    }
});

chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
    switch(request.func) {
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
        default:
            break;
    }
});

function sendMessageToContent(message) {
    chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
        chrome.tabs.sendMessage(tabs[0].id, message, function(response) {});
      });
}

function checkForUser() {
    if (document.cookie.includes('syncUID=')) {
        var cookie = document.cookie;
        return cookie.substring(cookie.indexOf('syncUID=') + 8);
    } else {
        var uid = ref.push().key;
        document.cookie = 'syncUID=' + uid + ';';
        return uid;
    }
}

function setupUser() {
    ref.child('users').child(user.uid).once('value').then(function(snapshot) {
        if (!snapshot.exists()) {
            ref.child('users').child(user.uid).set({ state: 0, name: "syncer" });
        }
    });

    ref.child('users').child(user.uid).on('value', function(snapshot) {
        user.name = snapshot.child('name').val();
        user.state = snapshot.child('state').val();

        setUI();
    });
}

function setUserName(name) {
    ref.child('users').child(user.uid).child('name').set(name);
}

function setUserState(state) {
    ref.child('users').child(user.uid).child('state').set(state);
}

function createServer(title) {
    var key = ref.child('servers').push().key;
    ref.child('servers').child(key).set({ title: title, size: 4, host: user.uid });
    ref.child('users').child(user.uid).child('server').update({ id: key, isHost: true })
    setUserState(1);
}

function setUI() {
    sendMessageToContent({ func: 'setUI', user: user });
}

function loadServers() {
    ref.child('servers').on('value', function(snapshot) {
        var servers = [];

        snapshot.forEach(function(childSnapshot) {
            var title = childSnapshot.child('title').val();
            var size = childSnapshot.child('size').val();

            servers.push({ title: title, size: size });
        });
        sendMessageToContent({ func: 'displayServers', servers: servers });
    });
}
    