const ref = firebase.database().ref();
const xmlHttp = new XMLHttpRequest();

var serverListListener, roomListener;

(function setupSemantic() {
    $('.ui.accordion').accordion();
})();

window.onbeforeunload = function () {
    leaveRoom();
}

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
        let serverList = document.getElementById('homeServers');
        serverList.innerHTML = '';
        rooms.forEach(function (room) {
            let div = document.createElement('div');
            let title = room.child('title').val();
            let media = room.child('media').child('title').val() != null ? room.child('media').child('title').val().substring(1) : 'No media';

            div.id = room.key;
            div.classList.add('item');
            div.innerHTML = '<h3>' + title + '</h3><p>' + media + '</p><button id="join_' + room.key + '" class="mini fluid ui iverted button">Join</button>';

            serverList.appendChild(div);
            document.getElementById('join_' + room.key).addEventListener('click', function (e) {
                e.preventDefault();

                let user = firebase.auth().currentUser;
                ref.child('users').child(user.uid).once('value').then(function (snapshot) {
                    xmlHttp.onreadystatechange = function () {
                        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                            roomListener = ref.child('rooms').child(room.key);
                            roomListener.on('value', function (snapshot) {
                                if (!snapshot.exists()) {
                                    alert('The sync room you were in was disbanded.');
                                    leaveRoom();
                                } else {
                                    setRoomUsers(snapshot.child('host').val(), snapshot.child('users'));
                                    setRoomMedia(snapshot.child('link').val(), snapshot.child('media'));
                                    setRoomMessages(snapshot.child('messages'));
                                }
                            });

                            $('#roomChatMessage').keypress(function (e) {
                                if (e.which == 13 && !e.shiftKey) {
                                    e.preventDefault();

                                    let message = document.getElementById('roomChatMessage').value;

                                    if (message.length > 0) {
                                        xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + room.key + '/sendMessage/' + message, true);
                                        xmlHttp.send();
                                    }

                                    return false;
                                }
                            });

                            setViewVisibility(1);
                        }
                    }
                    xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + snapshot.child('sessionId').val() + '/joinRoom/' + room.key, true);
                    xmlHttp.send();
                });

                return false;
            });
        });
    });
}

function setViewVisibility(level) {
    document.addEventListener('drop', function (e) {
        e.preventDefault();
        e.stopPropagation();
    });

    document.addEventListener('dragover', function (e) {
        e.preventDefault();
        e.stopPropagation();
    });

    switch (level) {
        case 1:
            $('.ui.sidebar').sidebar('hide');
            $('#home').fadeOut('fast', function () {
                $('#room').fadeIn('slow', function () {

                });
            });
            break;
        default:
            $('#room').fadeOut('fast', function () {
                $('#home').fadeIn('slow', function () {

                });
            });
            break;
    }
}

function setRoomUsers(host, users) {
    let userList = document.getElementById('roomUsers');
    userList.innerHTML = '';

    users.forEach(function (user) {
        if (host === user.key) {
            userList.innerHTML += '<div class="item"><div class="content"><div class="header"><a class="ui grey image label">' + user.val() + '<div class="detail">Host</div></a></div></div></div>';
        } else {
            userList.innerHTML += '<div class="item"><div class="content"><div class="header"><a class="ui grey image label">' + user.val() + '<i class="delete icon"></i></a></div></div></div>';
        }
    });
}

function setRoomMedia(link, media) {
    if (media.child('title').exists()) {
        let video = document.getElementById('roomVideo');
        let source = encodeURI(link + media.child('title').val());
        if (video.src !== source) {
            video.src = source;
            video.load();
        }
    }
}

function setRoomMessages(messages) {
    let messageList = document.getElementById('roomMessages');
    messageList.innerHTML = '';

    messages.forEach(function (message) {
        messageList.innerHTML += '<div class="item"><div class="content"><div class="header">' + message.child('sender').val() + '</div>' + message.child('body').val() + '</div></div>';
    });

    messageList.scrollTop = messageList.scrollHeight;
}

function createRoom(title) {
    let user = firebase.auth().currentUser;

    ref.child('users').child(user.uid).child('sessionId').once('value').then(function (sessionId) {
        if (sessionId.exists()) {
            var key;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                    if (key == null) {
                        key = xmlHttp.response;

                        roomListener = ref.child('rooms').child(key);
                        roomListener.on('value', function (snapshot) {
                            setRoomUsers(snapshot.child('host').val(), snapshot.child('users'));
                            setRoomMedia(snapshot.child('link').val(), snapshot.child('media'));
                            setRoomMessages(snapshot.child('messages'));
                        });

                        $('#roomChatMessage').keypress(function (e) {
                            if (e.which == 13 && !e.shiftKey) {
                                e.preventDefault();

                                let message = document.getElementById('roomChatMessage').value;

                                if (message.length > 0) {
                                    xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + key + '/sendMessage/' + message, true);
                                    xmlHttp.send();
                                    document.getElementById('roomChatMessage').value = '';
                                }

                                return false;
                            }
                        });

                        document.addEventListener('drop', function (e) {
                            e.preventDefault();
                            e.stopPropagation();

                            let file = e.dataTransfer.files[0];

                            if (file.type.includes('video/')
                                || file.type.includes('audio/')) {
                                let path = encodeURI(file.path);

                                while (path.includes('/')) {
                                    path = path.replace('/', '_____');
                                }

                                xmlHttp.onreadystatechange = function () {
                                    if (xmlHttp.readyState == 4 && xmlHttp.status == 401) {
                                        alert('Only the host can set the room media!');
                                    } else if (xmlHttp.readyState == 4 && xmlHttp.status == 404) {
                                        alert('Unable to set media!');
                                    }
                                };

                                xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + key + '/setRoomMedia/local/' + path, true);
                                xmlHttp.send();
                            } else {
                                alert(file.type)
                            }
                        });

                        document.addEventListener('dragover', function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                        });

                        setViewVisibility(1);
                    } else if (xmlHttp.readyState == 4 && xmlHttp.status == 403) {
                        alert('Unable to create room, please restart sync.');
                    }
                }
            }
            xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + sessionId.val() + '/createRoom/' + title, true);
            xmlHttp.send();
        } else {
            alert('Unable to create room, please restart sync.');
        }
    });
}

function setMedia(url) {
    let video = document.getElementById('roomVideo');
    video.src = url;
    video.load();
}

function leaveRoom() {
    if (roomListener != null) {
        roomListener.off();
    }

    let user = firebase.auth().currentUser;
    ref.child('users').child(user.uid).once('value').then(function (snapshot) {
        if (snapshot.child('state').val() > 0) {
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                    document.getElementById('roomVideo').src = '';
                    setViewVisibility(0);
                }
            }
            xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + snapshot.child('sessionId').val() + '/leaveRoom/' + snapshot.child('room').val(), true);
            xmlHttp.send();
        }
    });
}

document.getElementById('homeHost').addEventListener('click', function (e) {
    e.preventDefault();

    $('#homeCreateRoomModal').modal('show');

    return false;
});

document.getElementById('homeCreateRoom').addEventListener('click', function (e) {
    e.preventDefault();
    let title = document.getElementById('roomTitle');

    if (title.value.length === 0) {
        title.parentElement.classList.add('error');
    } else {
        title.parentElement.classList.remove('error');
        $('#homeCreateRoomModal').modal('hide');
        createRoom(title.value);
        title.value = '';
    }

    return false;
});

document.getElementById('homeJoin').addEventListener('click', function (e) {
    e.preventDefault();

    loadServers();
    $('.ui.sidebar').sidebar('toggle');

    return false;
});

document.getElementById('homeChangeUsername').addEventListener('click', function (e) {
    e.preventDefault();

    $('#homeChangeUsernameModal').modal('show');

    return false;
});

document.getElementById('homeChangeUsernameModalSet').addEventListener('click', function (e) {
    e.preventDefault();

    let newUsername = document.getElementById('newUsername');

    if (newUsername.value == 0) {
        newUsername.parentElement.classList.add('error');
    } else {
        newUsername.parentElement.classList.remove('error');
        let user = firebase.auth().currentUser;
        ref.child('users').child(user.uid).child('name').set(newUsername.value);
        updateUsername(newUsername.value);
        newUsername.value = '';
    }

    return false;
});

document.getElementById('roomLeave').addEventListener('click', function (e) {
    e.preventDefault();

    leaveRoom();

    return false;
})

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
            setViewVisibility(0);
            leaveRoom();
        });
    } else {
        authenticateUser();
    }
});
