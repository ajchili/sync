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