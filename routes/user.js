const express = require('express');
const router = express.Router();
const localtunnel = require('localtunnel');
const firebase = require('firebase');
const firebaseApp = firebase.initializeApp(require('../config/firebase_node.js'));
const ref = firebaseApp.database().ref();

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
                    link: tunnel.url
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

router.get('/:uid/:sessionId/closeRoom/:room', function (req, res) {
    try {
        localtunnel(3000, function (err, tunnel) {
            if (err) {
                return res.status(403).send(err);
            } else {
                ref.child('rooms').child(req.params.room).remove();

                ref.child('users').child(req.params.uid).update({
                    state: 0,
                    room: null,
                    sessionId: req.params.sessionId
                });
                
                return res.sendStatus(200);
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
                ref.child('users').child(req.params.uid).update({
                    state: 0,
                    room: null,
                    sessionId: req.params.sessionId
                });
                
                return res.sendStatus(200);
            }
        });
    } catch (err) {
        return res.status(403).send(err);
    }
});

module.exports = router;