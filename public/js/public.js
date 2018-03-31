const ref = firebase.database().ref();
const xmlHttp = new XMLHttpRequest();

$('.ui.accordion').accordion();

window.onbeforeunload = function () {
    let user = firebase.auth().currentUser;

    ref.child('users').child(user.uid).once('value').then(function (snapshot) {
        if (snapshot.child('room').exists()) {
            if (snapshot.child('state').val() === 1) {
                xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + snapshot.child('sessionId').val() + '/closeRoom/' + snapshot.child('room').val(), true);
                xmlHttp.send(null);
            } else if (snapshot.child('state').val() === 2) {
                xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + snapshot.child('sessionId').val() + '/leaveRoom/' + snapshot.child('room').val(), true);
                xmlHttp.send(null);
            }
        }
    })
};

function authenticateUser() {
    firebase.auth().signInAnonymously().catch(function (error) {
        var errorCode = error.code;
        var errorMessage = error.message;
        console.log(errorCode, errorMessage);
    });
}

function updateUsername(name) {
    document.getElementById('homeUsername').innerText = name;
}

function loadServers() {
    ref.child('rooms').once('value').then(function (rooms) {
        let serverList = document.getElementById('servers');
        serverList.innerHTML = '';
        rooms.forEach(function (room) {
            let div = document.createElement('div');
            let title = room.child('title').val();
            let media = room.child('media').val() != null ? room.child('media').val() : 'No media';

            div.id = room.key;
            div.classList.add('item');
            div.innerHTML = '<h3>' + title + '</h3><p>' + media + '</p><button  class="mini fluid ui iverted button">Join</button>';

            serverList.appendChild(div);
        });
    });
}

function setViewVisibility(level) {
    switch (level) {
        case 1:
            $('#home').fadeOut('fast', function () {

            });
            $('#room').fadeIn('slow', function () {

            });
            break;
        default:
            $('#room').fadeOut('fast', function () {

            });
            $('#home').fadeIn('fast', function () {

            });
            break;
    }
}

function createRoom(title) {
    let key = ref.child('rooms').push().key;
    let user = firebase.auth().currentUser;

    ref.child('users').child(user.uid).child('sessionId').once('value').then(function (sessionId) {
        if (sessionId.exists()) {
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                    let key = xmlHttp.response;
                    
                    setViewVisibility(1);
                } else if (xmlHttp.readyState == 4 && xmlHttp.status == 403) {
                    alert('Unable to create room, please restart sync.');
                }
            }
            xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + sessionId.val() + '/createRoom/' + title, true);
            xmlHttp.send(null);
        } else {
            alert('Unable to create room, please restart sync.');
        }
    });
}

function checkIfInRoom() {
    let user = firebase.auth().currentUser
    ref.child('users').child(user.uid).child('room').once('value').then(function (room) {
        if (room.exists()) {
            ref.child('rooms').child(room.val()).once('value').then(function (room) {
                if (room.exists()) {
                    setViewVisibility(1);
                } else {
                    ref.child('users').child(user.uid).update({
                        state: 0,
                        room: null,
                        link: null
                    });
                    setViewVisibility(0);
                }
            });
        } else {
            setViewVisibility(0);
        }
    });
}

document.getElementById('host').addEventListener('click', function (e) {
    e.preventDefault();

    $('#homeCreateRoomModal').modal({
        blurring: true
    }).modal('show');

    return false;
});

document.getElementById('homeCreateRoom').addEventListener('click', function (e) {
    e.preventDefault();
    let title = document.getElementById('roomTitle')

    if (title.value.length === 0) {
        title.parentElement.classList.add('error');
    } else {
        title.parentElement.classList.remove('error');
        $('#homeCreateRoomModal').modal('hide');
        createRoom(title.value);
    }

    return false;
});

document.getElementById('join').addEventListener('click', function (e) {
    e.preventDefault();

    loadServers();
    $('.ui.sidebar').sidebar('toggle');

    return false;
});

document.getElementById('homeChangeUsername').addEventListener('click', function (e) {
    e.preventDefault();

    $('#homeChangeUsernameModal').modal({
        blurring: true
    }).modal('show');

    return false;
});

document.getElementById('homeChangeUsernameModalSet').addEventListener('click', function (e) {
    e.preventDefault();

    let newUsername = document.getElementById('newUsername').value;
    let user = firebase.auth().currentUser;

    ref.child('users').child(user.uid).child('name').set(newUsername);

    updateUsername(newUsername);

    return false;
});

firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        ref.child('users').child(user.uid).once('value').then(function (snapshot) {
            let sessionId = ref.child('users').child(user.uid).child('sessionId').push().key;
            if (!snapshot.child('name').exists()) {
                ref.child('users').child(user.uid).set({
                    name: 'syncer',
                    sessionId: sessionId,
                    state: 0
                });
                updateUsername('syncer');
            } else {
                ref.child('users').child(user.uid).update({
                    sessionId: sessionId
                });
                updateUsername(snapshot.child('name').val());
            }
            checkIfInRoom();
        });
    } else {
        authenticateUser();
    }
});
