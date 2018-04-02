const ref = firebase.database().ref();
const xmlHttp = new XMLHttpRequest();

var serverListListener, roomListener, roomMediaListener, roomUserListener, roomMessageListener, videoTimeInterval;

/*
    Name: onbeforeunload
    Purpose: Leave current room if electron is refreshed.
*/
window.onbeforeunload = function () {
    leaveRoom();
}

/*
    Name: authenticateUser
    Purpose: Sign in the user
*/
function authenticateUser() {
    firebase.auth().signInAnonymously().catch(function (error) {
        var errorCode = error.code;
        var errorMessage = error.message;
        console.log(errorCode, errorMessage);
    });
}

/*
    Name: updateUserName
    Purpose: Set the username span to display username and if a new username is provided, update it in Firebase.
    Params:
        name: username
*/
function updateUsername(name) {
    let user = firebase.auth().currentUser;
    ref.child('users').child(user.uid).child('name').set(name);
    document.getElementById('homeUsername').innerText = name;
}

/*
    Name: loadServers
    Purpose: Load servers to be displayed in server list.
*/
function loadServers() {
    ref.child('rooms').once('value').then(function (rooms) {
        let serverList = document.getElementById('homeServers');
        serverList.innerHTML = '';
        rooms.forEach(function (room) {
            let div = document.createElement('div');
            let title = room.child('title').val();
            let media = room.child('media').child('title').val() != null ? room.child('media').child('title').val() : 'No media';

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
                                    roomListener.off();
                                    alert('The sync room you were in was disbanded.');
                                    leaveRoom();
                                }
                            });

                            ref.child('rooms').child(room.key).once('value').then(function (room) {
                                roomMediaListener = ref.child('rooms').child(room.key).child('media');
                                roomMediaListener.on('value', function (media) {
                                    setRoomMedia(room.child('link').val(), media, false);
                                });

                                roomUserListener = ref.child('rooms').child(room.key).child('users');
                                roomUserListener.on('value', function (users) {
                                    setRoomUsers(room.child('host').val(), users);
                                });

                                roomMessageListener = ref.child('rooms').child(room.key).child('messages');
                                roomMessageListener.on('value', function (messages) {
                                    setRoomMessages(messages);
                                });
                            });

                            $('#roomChatMessage').off();
                            $('#roomChatMessage').keypress(function (e) {
                                if (e.which == 13 && !e.shiftKey) {
                                    e.preventDefault();

                                    let message = document.getElementById('roomChatMessage').value;

                                    if (message.length > 0) {
                                        xmlHttp.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + room.key + '/sendMessage/' + message, true);
                                        xmlHttp.send();
                                        document.getElementById('roomChatMessage').value = '';
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

/* 
    Name: setRoomUsers
    Purpose: Display the users that are in a room in the user list.
    Parmas:
        host: host uid
        users: snapshot of users in room
*/
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

/*
    Name: setRoomMedia
    Purpose: Set the media that the room is currently playing.
    Params:
        link: url that is associated with room
        media: title of media
        isHost: is host of room
*/
function setRoomMedia(link, media, isHost) {
    let player = videojs('roomVideo')
    if (media.child('title').exists()) {
        let url = encodeURI(link + media.child('title').val())
        if (player.currentSrc() !== url) {
            player.src(url);
        }
    }

    if (!isHost) {
        if (media.child('paused').exists()) {
            if (media.child('paused').val()) {
                player.pause();
            } else {
                if (media.child('time').exists()) {
                    let currentTime = player.currentTime();
                    let mediaTime = media.child('time').val();
                    if (currentTime < mediaTime - 1 || currentTime > mediaTime + 1) {
                        player.currentTime(mediaTime);
                    }
                }
                player.play();
            }
        }
    }
}

/*
    Name: setRoomMessages
    Purpose: Display the messages that have been sent.
    Params:
        messages: snapshot of messages sent in room
*/
function setRoomMessages(messages) {
    let messageList = document.getElementById('roomMessages');
    messageList.innerHTML = '';

    messages.forEach(function (message) {
        messageList.innerHTML += '<div class="item"><div class="content"><div class="header">' + message.child('sender').val() + '</div>' + message.child('body').val() + '</div></div>';
    });

    messageList.scrollTop = messageList.scrollHeight;
}

/*
    Name: setRoomVideoEvents
    Purpose: Set event listeners for video events.
    Params:
        room: room id
*/
function setRoomVideoEvents(room) {
    let player = videojs('roomVideo')

    clearInterval(videoTimeInterval);
    videoTimeInterval = setInterval(function () {
        ref.child('rooms').child(room).child('media').child('time').set(player.currentTime());
    }, 200);
    player.on('pause', function () {
        ref.child('rooms').child(room).child('media').child('paused').set(true)
    });
    player.on('play', function () {
        ref.child('rooms').child(room).child('media').child('paused').set(false)
    });
}

/*
    Name: createRoom
    Purpose: Interacts with nodejs backend to create room.
    Params:
        title: title of room
*/
function createRoom(title) {
    let user = firebase.auth().currentUser;

    ref.child('users').child(user.uid).child('sessionId').once('value').then(function (sessionId) {
        if (sessionId.exists()) {
            var key;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                    if (key == null) {
                        key = xmlHttp.response;

                        ref.child('rooms').child(key).once('value').then(function (room) {
                            roomMediaListener = ref.child('rooms').child(key).child('media');
                            roomMediaListener.on('value', function (media) {
                                setRoomMedia(room.child('link').val(), media, true);
                            });

                            roomUserListener = ref.child('rooms').child(key).child('users');
                            roomUserListener.on('value', function (users) {
                                setRoomUsers(room.child('host').val(), users);
                            });

                            roomMessageListener = ref.child('rooms').child(key).child('messages');
                            roomMessageListener.on('value', function (messages) {
                                setRoomMessages(messages);
                            });

                            setRoomVideoEvents(key);
                        });

                        $('#roomChatMessage').off();
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

                            if (file.type.includes('video/')) {
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

/*
    Name: leaveRoom
    Purpose: Interacts with nodejs backend to leave or disband current room depending on if the user is the host.
*/
function leaveRoom() {
    if (roomListener != null) {
        roomListener.off();
    }

    if (roomMediaListener != null) {
        roomMediaListener.off();
    }

    if (roomUserListener != null) {
        roomUserListener.off();
    }

    if (roomMessageListener != null) {
        roomMessageListener.off();
    }

    clearInterval(videoTimeInterval);

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

/*
Name: onAuthStateChanged
Purpose: Setup sync when a user is authenticated or create a user if a user does not already exist.
*/
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
