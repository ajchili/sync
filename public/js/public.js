const ref = firebase.database().ref();

function authenticateUser() {
    firebase.auth().signInAnonymously().catch(function (error) {
        var errorCode = error.code;
        var errorMessage = error.message;
        console.log(errorCode, errorMessage);
    });
}

function updateUsername(name) {
    document.getElementById('homeUsername').innerText = name
}

function loadServers() {
    ref.child('rooms').once('value').then(function (rooms) {
        let serverList = document.getElementById('servers');
        serverList.innerHTML = '';
        rooms.forEach(function (room) {
            let div = document.createElement('div');
            div.id = room.key;
            div.classList.add('item');
            let title = room.child('title').val()
            let media = room.child('media').val() != null ? room.child('media').val() : 'No media'
            div.innerHTML = '<h3>' + title + '</h3><p>' + media + '</p><button  class="mini fluid ui iverted button">Join</button>';
            serverList.appendChild(div);
        });
    });
}

document.getElementById('host').addEventListener('click', function (e) {
    e.preventDefault();

    $('#homeCreateRoomModal').modal('show');
    
    return false;
});

document.getElementById('homeCreateRoom').addEventListener('click', function (e) {
    e.preventDefault();
    let title = document.getElementById('roomTitle')

    if (title.value.length < 3) {
        title.classList.add('error')
    } else {
        title.classList.remove('error')
        let key = ref.child('rooms').push().key
        let user = firebase.auth().currentUser
        ref.child('rooms').child(key).set({
            title: title.value,
            host: user.uid
        })
    }
    return false;
})

document.getElementById('join').addEventListener('click', function (e) {
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

    let newUsername = document.getElementById('newUsername').value
    let user = firebase.auth().currentUser
    ref.child('users').child(user.uid).child('name').set(newUsername);
    updateUsername(newUsername);
    return false;
});

firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        ref.child('users').child(user.uid).once('value').then(function (snapshot) {
            if (!snapshot.child('name').exists()) {
                ref.child('users').child(user.uid).set({
                    name: 'syncer',
                    state: 0
                });
                updateUsername('syncer');
            } else {
                updateUsername(snapshot.child('name').val());
            }
        });
    } else {
        authenticateUser();
    }
});
