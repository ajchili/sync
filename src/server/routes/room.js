const express = require("express");
const generator = require("generate-password");
const router = express.Router();
const SocketHandler = require("../utils/SocketHandler");
const Tunneler = require("../utils/Tunneler");

const generateBearer = () => {
  return generator.generate({
    length: 128
  });
};

const blacklist = ["/create", "/socketTunnel"];

module.exports = (server, port = 8080) => {
  let tunneler = new Tunneler();
  let socketHandler = new SocketHandler();
  let bearer = null;

  router.use((req, res, next) => {
    if (blacklist.includes(req.path)) return next();
    else if (req.headers.authorization) {
      if (req.headers.authorization.split(" ").length) {
        let requestBearer = req.headers.authorization.split(" ")[1];
        if (bearer === requestBearer) return next();
      }
    }
    res.sendStatus(401);
  });

  router.post("/close", async (_, res) => {
    if (tunneler.isActive()) {
      bearer = null;
      socketHandler.stop();
      try {
        await tunneler.closeTunnel();
        res.sendStatus(200);
      } catch (err) {
        console.error("Unable to close room:", err);
        res.status(500).json(err);
      }
    } else {
      console.error("Attempting to close room that does not exist!");
      res.sendStatus(400);
    }
  });

  router.post("/create", async (_, res) => {
    if (tunneler.isActive()) {
      console.error("Room already exists!");
      res.status(400).json({ url: tunneler.url });
    } else {
      try {
        let url = await tunneler.createTunnel(port);
        socketHandler.start(server);
        bearer = generateBearer();
        res.status(200).json({ url, bearer });
      } catch (err) {
        console.error("Unable to create room:", err);
        res.status(500).json(err);
      }
    }
  });

  router.post("/setMedia", (req, res) => {
    if (req.body.url) {
      socketHandler.setMedia(req.body.url);
      res.sendStatus(200);
    } else res.sendStatus(400);
  });

  router.post("/uploadMedia", (req, res) => {
    if (req.files && req.files.media) {
      let file = req.files.media;
      file.mv(`./media/${file.name}`, err => {
        if (err) return res.status(500).json(err);
        if (tunneler.isActive()) {
          let mediaURL = `${tunneler.url}/media/${file.name}`;
          socketHandler.setMedia(mediaURL);
        }
        res.sendStatus(200);
      });
    } else res.sendStatus(400);
  });

  router.get("/socketTunnel", (req, res) => {
    if (tunneler.isActive()) res.status(200).send(tunneler.url);
    else res.sendStatus(400);
  });

  return router;
};
