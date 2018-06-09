const urlRegex = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;

/*
    Name: setupSemantic
    Purpose: Setup semantic jquery so that UI elements that rely on it can work properly
*/
(function setupSemantic() {
    $('.ui.accordion').accordion();
})();

/*
    Name: setViewVisibility
    Purpose: Set which view is currently displayed to user.
    Params:
        level: 0 - home
               1 - room
*/
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
                    hideDimmer();
                });
            });
            break;
        default:
            $('#room').fadeOut('fast', function () {
                $('#home').fadeIn('slow', function () {
                    hideDimmer();
                });
            });
            break;
    }
}

/*
    Name: setMedia
    Purpose: Set the current source of the video.
    Params:
        url: source url
*/
function setMedia(url) {
    let video = document.getElementById('roomVideo');
    video.src = url;
    video.load();
}

/*
    Name: urlify
    Source: https://stackoverflow.com/a/8943487
    Purpose: Return a string with wrapped urls for display in a chat.
    Params:
        text: message
*/
function urlify(text) {
    return text.replace(urlRegex, function (url) {
        return `<a href="${url}" target="_blank">${url}</a>`;
    });
}

/*
    Name: showDimmer
    Purpose: Displays loading icon and disables UI until hidden.
    Params:
        text: message
*/
function showDimmer(text) {
    let dimmer = document.getElementById('dimmer');
    let dimmerText = document.getElementById('dimmerText');
    dimmerText.innerHTML = text;
    dimmer.classList.add('active');
}

/*
    Name: hideDimmer
    Purpose: Hides loading icon and reenables UI.
*/
function hideDimmer() {
    let dimmer = document.getElementById('dimmer');
    let dimmerText = document.getElementById('dimmerText');
    dimmerText.innerHTML = '';
    dimmer.classList.remove('active');
} 