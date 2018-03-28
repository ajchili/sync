const ref = firebase.database().ref();

function loadServers() {
    
}

document.getElementById('join').addEventListener('click', function (e) {
    e.preventDefault();

    $('.ui.sidebar').sidebar('toggle');

    return false;
})