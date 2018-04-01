const express = require('express');
const router = express.Router();
const localtunnel = require('localtunnel');
const fs = require('fs-extra');
const firebase = require('firebase');
const firebaseApp = firebase.initializeApp(require('../config/firebase_node.js'));
const ref = firebaseApp.database().ref();

// https://stackoverflow.com/a/30405105
function moveFileToMediaFolder(file) {
    let location = process.platform !== 'darwin' ? __dirname.substring(0, __dirname.lastIndexOf('\\')) + '\\media' : __dirname.substring(0, __dirname.lastIndexOf('/')) + '/media';
    let readStream = fs.createReadStream(file);
    fs.ensureDir(location, null);
    let writeStream = fs.createWriteStream(location + file.substring(file.lastIndexOf(process.platform !== 'darwin' ? '\\' : '/'), file.length));

    return new Promise(function (resolve, reject) {
        readStream.on('error', reject);
        readStream.on('error', reject);
        writeStream.on('finish', resolve);
        readStream.pipe(writeStream);
    }).catch(function (err) {
        readStream.destroy();
        writeStream.end();
        throw err;
    });
}

router.get('/:uid/:sessionId/createRoom/:title', function (req, res) {
    try {
        localtunnel(3000, function (err, tunnel) {
            if (err) {
                return res.status(403).send(err);
            } else {
                let key = ref.child('rooms').push().key;

                ref.child('rooms').child(key).set({
                    host: req.params.uid,
                    title: req.params.title,
                    link: tunnel.url + '/media/'
                });

                ref.child('users').child(req.params.uid).update({
                    state: 1,
                    room: key,
                    sessionId: req.params.sessionId
                });

                return res.status(200).send(key);
            }
        });
    } catch (err) {
        return res.status(403).send(err);
    }
});

router.get('/:uid/:sessionId/joinRoom/:room', function (req, res) {
    try {
        localtunnel(3000, function (err, tunnel) {
            if (err) {
                return res.status(403).send(err);
            } else {
                ref.child('rooms').child(req.params.room).once('value').then(function (room) {
                    if (room.exists()) {
                        ref.child('users').child(req.params.uid).update({
                            state: 2,
                            room: room.key,
                            sessionId: req.params.sessionId
                        });

                        return res.sendStatus(200);
                    } else {
                        return res.sendStatus(404);
                    }
                });
            }
        });
    } catch (err) {
        return res.status(403).send(err);
    }
});

router.get('/:uid/:sessionId/leaveRoom/:room', function (req, res) {
    try {
        localtunnel(3000, function (err, tunnel) {
            if (err) {
                return res.status(403).send(err);
            } else {
                ref.child('rooms').child(req.params.room).once('value').then(function (room) {
                    if (room.exists() && room.child('host').val() === req.params.uid) {
                        ref.child('rooms').child(req.params.room).remove();
                    }

                    ref.child('users').child(req.params.uid).update({
                        state: 0,
                        room: null,
                        sessionId: req.params.sessionId
                    });

                    return res.sendStatus(200);
                });
            }
        });
    } catch (err) {
        return res.status(403).send(err);
    }
});

router.get('/:uid/:room/setRoomMedia/:url', function (req, res) {
    return res.sendStatus(200);
});

router.get('/:uid/:room/setRoomMedia/local/:path', function (req, res) {
    let path = decodeURI(req.params.path);

    while (path.includes('_____')) {
        path = path.replace('_____', '/');
    }

    moveFileToMediaFolder(path).then(function () {
        let fileName = path.substring(path.lastIndexOf(process.platform !== 'drawin' ? '\\' : '/', path.length));

        ref.child('rooms').child(req.params.room).once('value').then(function (room) {
            if (room.exists() && room.child('host').val() === req.params.uid) {
                ref.child('rooms').child(req.params.room).child('media').set({
                    title: fileName,
                    paused: true,
                    time: 0
                });

                return res.status(200).send(room.child('link').val() + fileName);
            } else {
                return res.sendStatus(401);
            }
        });
    }).catch(function (err) {
        console.log(err)
        return res.status(404).send(err);
    });
});

module.exports = router;