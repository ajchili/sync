const ref = firebase.database().ref();

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

                showDimmer('Joining server...');

                ref.child('users').child(user.uid).once('value').then(function (snapshot) {
                    let joinRequest = new XMLHttpRequest();
                    joinRequest.onreadystatechange = function () {
                        if (joinRequest.readyState == 4 && joinRequest.status == 200) {
                            roomListener = ref.child('rooms').child(room.key);
                            roomListener.on('value', function (snapshot) {
                                if (!snapshot.exists()) {
                                    roomListener.off();
                                    let player = videojs('roomVideo');
                                    player.pause();
                                    $('#roomDisbandedModal').modal({
                                        onHidden: function () {
                                            leaveRoom();
                                        }
                                    }).modal('show');
                                }
                            });

                            createPlayer();

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

                                setRoomVideoEvents(room.key, false);
                                setRoomSettingsEvents(room.key, false);
                            });

                            $('#roomChatMessage').off();
                            $('#roomChatMessage').keypress(function (e) {
                                if (e.which == 13 && !e.shiftKey) {
                                    e.preventDefault();

                                    let message = urlify(document.getElementById('roomChatMessage').value);

                                    while (message.includes('/')) {
                                        message = message.replace('/', '_____');
                                    }

                                    if (message.length > 0) {
                                        let messageRequest = new XMLHttpRequest();
                                        messageRequest.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + room.key + '/sendMessage/' + message, true);
                                        messageRequest.send();
                                        document.getElementById('roomChatMessage').value = '';
                                    }

                                    return false;
                                }
                            });

                            setViewVisibility(1);
                        }
                    }
                    joinRequest.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + snapshot.child('sessionId').val() + '/joinRoom/' + room.key, true);
                    joinRequest.send();
                });

                return false;
            });
        });
    });
}

/* 
    Name: createPlayer
    Purpose: Creates videojs player when user connects to a room.
*/
function createPlayer() {
    let roomVideo = '<video id="roomVideo" class="video-js" controls preload="auto" style="width: 100%; height: 100%; float: left;" data-setup="{}"></video>';
    document.getElementById('roomVideoHolder').innerHTML = roomVideo;
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
    let player = videojs('roomVideo');

    if (media.child('title').exists()
        && media.child('type').exists()
        && media.child('title').val().length > 0
        && media.child('type').val().length > 0) {
        let url = encodeURI(link + media.child('title').val());
        let type = media.child('type').val();
        if (player.currentSrc() !== url) {
            player.src({
                type: type,
                src: url
            });
        }
    }

    if (!isHost) {
        if (media.child('paused').exists()) {
            if (media.child('paused').val()) {
                player.pause();
            } else {
                player.play();
            }
        }

        if (media.child('time').exists()) {
            let currentTime = player.currentTime();
            let mediaTime = media.child('time').val();
            if (currentTime < mediaTime - 1 || currentTime > mediaTime + 1) {
                player.currentTime(mediaTime);
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
        isHost: is host of room
*/
function setRoomVideoEvents(room, isHost) {
    let player = videojs('roomVideo');

    clearInterval(videoTimeInterval);

    if (isHost) {
        videoTimeInterval = setInterval(function () {
            ref.child('rooms').child(room).child('media').child('time').set(player.currentTime());
        }, 200);

        player.on('pause', function () {
            ref.child('rooms').child(room).child('media').child('paused').set(true);
        });

        player.on('play', function () {
            ref.child('rooms').child(room).child('media').child('paused').set(false);
        });
    } else {
        player.on('play', function () {
            ref.child('rooms').child(room).child('media').child('paused').once('value').then(function (isMediaPaused) {
                if (isMediaPaused.exists() && isMediaPaused.val()) {
                    player.pause();
                }
            });
        });
    }
}

/*
    Name: setRoomSettingsEvents
    Purpose: Sets up UI elements to properly interact with the room.
    Params:
        room: room id
        isHost: is host of room
*/
function setRoomSettingsEvents(room, isHost) {
    let settingsDropdown = document.getElementById('roomSettingsDropdown');
    settingsDropdown.hidden = !isHost;
}

/*
    Name: createRoom
    Purpose: Interacts with nodejs backend to create room.
    Params:
        title: title of room
*/
function createRoom(title) {
    let user = firebase.auth().currentUser;

    showDimmer('Creating server...');

    ref.child('users').child(user.uid).child('sessionId').once('value').then(function (sessionId) {
        if (sessionId.exists()) {
            let createRequest = new XMLHttpRequest();
            createRequest.onreadystatechange = function () {
                if (createRequest.readyState == 4 && createRequest.status == 200) {
                    let key = createRequest.response;

                    ref.child('rooms').child(key).once('value').then(function (room) {
                        createPlayer();

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

                        setRoomVideoEvents(key, true);
                        setRoomSettingsEvents(key, true);
                    });

                    $('#roomChatMessage').off();
                    $('#roomChatMessage').keypress(function (e) {
                        if (e.which == 13 && !e.shiftKey) {
                            e.preventDefault();

                            let message = urlify(document.getElementById('roomChatMessage').value);

                            while (message.includes('/')) {
                                message = message.replace('/', '_____');
                            }

                            if (message.length > 0) {
                                let messageRequest = new XMLHttpRequest();
                                messageRequest.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + key + '/sendMessage/' + message, true);
                                messageRequest.send();
                                document.getElementById('roomChatMessage').value = '';
                            }

                            return false;
                        }
                    });

                    document.addEventListener('drop', function (e) {
                        e.preventDefault();
                        e.stopPropagation();

                        showDimmer('Moving media...');

                        let file = e.dataTransfer.files[0];

                        let path = encodeURI(file.path);
                        let type = encodeURI(file.type);

                        while (path.includes('/')) {
                            path = path.replace('/', '_____');
                        }

                        while (type.includes('/')) {
                            type = type.replace('/', '_____');
                        }

                        let mediaRequest = new XMLHttpRequest();
                        mediaRequest.onreadystatechange = function () {
                            if (mediaRequest.readyState == 4) {
                                hideDimmer();
                            }
                            
                            if (mediaRequest.readyState == 4 && mediaRequest.status == 401) {
                                alert('Only the host can set the room media!');
                            } else if (mediaRequest.readyState == 4 && mediaRequest.status == 404) {
                                alert('Unable to set media!');
                            }
                        };
                        mediaRequest.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + key + '/setRoomMedia/' + type + '/' + path, true);
                        mediaRequest.send();
                    });

                    document.addEventListener('dragover', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                    });

                    setViewVisibility(1);
                } else if (createRequest.readyState == 4 && createRequest.status == 403) {
                    alert('Unable to create room, please restart sync.');
                }
            }
            createRequest.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + sessionId.val() + '/createRoom/' + title, true);
            createRequest.send();
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
    showDimmer('Leaving room...');

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
            let leaveRequest = new XMLHttpRequest();
            leaveRequest.onreadystatechange = function () {
                if (leaveRequest.readyState == 4 && leaveRequest.status == 200) {
                    if (videojs('roomVideo')) {
                        player = videojs('roomVideo');
                        player.dispose();
                    }
                    setViewVisibility(0);
                }
            }
            leaveRequest.open("GET", 'http://localhost:3000/user/' + user.uid + '/' + snapshot.child('sessionId').val() + '/leaveRoom/' + snapshot.child('room').val(), true);
            leaveRequest.send();
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

            if (snapshot.child('room').exists()) {
                leaveRoom();
            }

            setViewVisibility(0);
        });
    } else {
        authenticateUser();
    }
});
