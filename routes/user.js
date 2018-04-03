const express = require('express');
const router = express.Router();
const localtunnel = require('localtunnel');
const fs = require('fs-extra');
const firebase = require('firebase');
const firebaseApp = firebase.initializeApp(require('../config/firebase_node.js'));
const ref = firebaseApp.database().ref();

var url;

// https://stackoverflow.com/a/30405105
function moveFileToMediaFolder(file) {
    let location = process.platform !== 'darwin' ? __dirname.substring(0, __dirname.lastIndexOf('\\')) + '\\media' : __dirname.substring(0, __dirname.lastIndexOf('/')) + '/media';
    if (file === location + file.substring(file.lastIndexOf(process.platform !== 'darwin' ? '\\' : '/'))) {
        return new Promise(function (resolve, reject) {
            resolve();
        });
    } else {
        let readStream = fs.createReadStream(file);
        fs.ensureDir(location, null);
        let writeStream = fs.createWriteStream(location + file.substring(file.lastIndexOf(process.platform !== 'darwin' ? '\\' : '/')));

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
}

function createRoom (req, res) {
    let key = ref.child('rooms').push().key;

    ref.child('rooms').child(key).set({
        host: req.params.uid,
        title: req.params.title,
        link: url + '/media/'
    });

    ref.child('users').child(req.params.uid).update({
        state: 1,
        room: key,
        sessionId: req.params.sessionId
    });

    ref.child('users').child(req.params.uid).child('name').once('value').then(function (username) {
        ref.child('rooms').child(key).child('users').child(req.params.uid).set(username.val());

        return res.status(200).send(key);
    });
}

router.get('/:uid/:sessionId/createRoom/:title', function (req, res) {
    try {
        if (url == null) {
            localtunnel(3000, function (err, tunnel) {
                if (err) {
                    return res.status(403).send(err);
                } else {
                    url = tunnel.url;
                    createRoom(req, res);
                }
            });
        } else {
            createRoom(req, res);
        }
    } catch (err) {
        return res.status(403).send(err);
    }
});

router.get('/:uid/:sessionId/joinRoom/:room', function (req, res) {
    ref.child('rooms').child(req.params.room).once('value').then(function (room) {
        if (room.exists()) {
            ref.child('users').child(req.params.uid).update({
                state: 2,
                room: room.key,
                sessionId: req.params.sessionId
            });

            ref.child('users').child(req.params.uid).child('name').once('value').then(function (username) {
                ref.child('rooms').child(room.key).child('users').child(req.params.uid).set(username.val());
        
                return res.sendStatus(200);
            });
        } else {
            return res.sendStatus(404);
        }
    });
});

router.get('/:uid/:sessionId/leaveRoom/:room', function (req, res) {
    ref.child('rooms').child(req.params.room).once('value').then(function (room) {
        if (room.exists()) {
            if (room.child('host').val() === req.params.uid) {
                ref.child('rooms').child(req.params.room).remove();
            } else {
                ref.child('rooms').child(req.params.room).child('users').child(req.params.uid).remove();
            }
        }

        ref.child('users').child(req.params.uid).update({
            state: 0,
            room: null,
            sessionId: req.params.sessionId
        });

        return res.sendStatus(200);
    });
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
        let fileName = path.substring(path.lastIndexOf(process.platform !== 'darwin' ? '\\' : '/') + 1);

        ref.child('rooms').child(req.params.room).once('value').then(function (room) {
            if (room.exists() && room.child('host').val() === req.params.uid) {
                ref.child('rooms').child(req.params.room).child('media').set({
                    title: fileName,
                    paused: true,
                    time: 0
                });

                return res.sendStatus(200);
            } else {
                return res.sendStatus(401);
            }
        });
    }).catch(function (err) {
        console.log(err);
        return res.status(404).send(err);
    });
});

router.get('/:uid/:room/sendMessage/:message', function (req, res) {
    let message = decodeURI(req.params.message);
    
    while (message.includes('_____')) {
        message = message.replace('_____', '/');
    }

    ref.child('users').child(req.params.uid).child('name').once('value').then(function (username) {
        let messageId = ref.child('rooms').child(req.params.room).child('messages').push().key;

        ref.child('rooms').child(req.params.room).child('users').child(req.params.uid).once('value').then(function (user) {
            if (user.exists()) {
                ref.child('rooms').child(req.params.room).child('messages').child(messageId).set({
                    sender: username.val(),
                    body: message
                });
        
                return res.sendStatus(200);
            } else {
                return res.sendStatus(401);
            }
        })
    });
});

module.exports = router;