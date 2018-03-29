const ref = firebase.database().ref();

function authenticateUser() {
    firebase.auth().signInAnonymously().catch(function (error) {
        // Handle Errors here.
        var errorCode = error.code;
        var errorMessage = error.message;
        console.log(errorCode, errorMessage);
    });
}

function loadServers() {
    ref.child('rooms').once('value').then(function (rooms) {
        let serverList = document.getElementById('servers');
        serverList.innerHTML = '';
        rooms.forEach(function (room) {
            let div = document.createElement('div');
            div.id = room.key;
            div.classList.add('item');
            div.innerHTML = '<h3>' + room.child('title').val() + '</h3><p>' + room.child('media').val() + '</p><button  class="mini fluid ui iverted button">Join</button>';
            serverList.appendChild(div);
        });
    });
}

document.getElementById('join').addEventListener('click', function (e) {
    e.preventDefault();

    loadServers();
    $('.ui.sidebar').sidebar('toggle');

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
            }
        });
    } else {
        authenticateUser();
    }
});
