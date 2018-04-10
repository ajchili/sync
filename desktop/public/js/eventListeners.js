/*
    Name: homeHost click
    Purpose: Show create room modal.
*/
document.getElementById('homeHost').addEventListener('click', function (e) {
    e.preventDefault();

    $('#homeCreateRoomModal').modal('show');

    return false;
});

/*
    Name: homeCreateRoom click
    Purpose: Create room with provided title, however, if no title is provided, display error.
*/
document.getElementById('homeCreateRoom').addEventListener('click', function (e) {
    e.preventDefault();
    let title = document.getElementById('roomTitle');
    let password = document.getElementById('homeCreateRoomModalPrivatePassword');

    if (title.value.length === 0) {
        title.parentElement.classList.add('error');
    } else {
        title.parentElement.classList.remove('error');
        $('#homeCreateRoomModal').modal('hide');
        createRoom(title.value, password.value);
        title.value = '';
        password.value = '';
    }

    return false;
});

/*
    Name: roomTitle keyup
    Purpose: Create room with provided title on enter, however, if no title is provided, display error.
*/
$('#roomTitle').on('keyup', function (e) {
    if (e.keyCode == 13) {
        let title = document.getElementById('roomTitle');
    
        if (title.value.length === 0) {
            title.parentElement.classList.add('error');
        } else {
            title.parentElement.classList.remove('error');
            $('#homeCreateRoomModal').modal('hide');
            createRoom(title.value);
            title.value = '';
        }
    }
});

/*
    Name: homeJoin click
    Purpose: Show server list.
*/
document.getElementById('homeJoin').addEventListener('click', function (e) {
    e.preventDefault();

    loadServers();
    $('.ui.sidebar').sidebar('toggle');

    return false;
});

/*
    Name: homeChangeUsername click
    Purpose: Show change username modal.
*/
document.getElementById('homeChangeUsername').addEventListener('click', function (e) {
    e.preventDefault();

    $('#homeChangeUsernameModal').modal('show');

    return false;
});

/*
    Name: homeChangeUsernameModalSet click
    Purpose: Update username with provided username, however, if no username is provided, display error.
*/
document.getElementById('homeChangeUsernameModalSet').addEventListener('click', function (e) {
    e.preventDefault();

    let newUsername = document.getElementById('newUsername');

    if (newUsername.value == 0) {
        newUsername.parentElement.classList.add('error');
    } else {
        newUsername.parentElement.classList.remove('error');
        $('#homeChangeUsernameModal').modal('hide');
        updateUsername(newUsername.value);
        newUsername.value = '';
    }

    return false;
});

/*
    Name: roomLeave click
    Purpose: Leave current room.
*/
document.getElementById('roomLeave').addEventListener('click', function (e) {
    e.preventDefault();

    leaveRoom();

    return false;
});

/*
    Name: homeAdInfo click
    Purpose: Show ad info modal.
*/
document.getElementById('homeAdInfo').addEventListener('click', function (e) {
    e.preventDefault();

    $('#homeAdInfoModal').modal('show');

    return false;
});

/*
    Name: homeCreateRoomModalPrivatePassword keyup
    Purpose: Update the password icon to display if room is locked or unlocked depending on if password is provided.
*/
$('#homeCreateRoomModalPrivatePassword').on('keyup', function (e) {
    if (e.keyCode != 13) {
        let password = document.getElementById('homeCreateRoomModalPrivatePassword').value;
        let icon = document.getElementById('homeCreateRoomModalPrivateButtonIcon');
        let button = document.getElementById('homeCreateRoomModalPrivateButton');
    
        if (password.length === 0) {
            icon.classList.remove('lock');
            icon.classList.add('unlock');
            button.classList.remove('active');
        } else {
            icon.classList.remove('unlock');
            icon.classList.add('lock');
            button.classList.add('active');
        }
    }
});