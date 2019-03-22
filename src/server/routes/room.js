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

  router.post("/close", (req, res) => {
    if (tunneler.isActive()) {
      bearer = null;
      socketHandler.stop();
      setTimeout(() => {
        tunneler.closeTunnel();
        res.sendStatus(200);
      }, 1500);
    } else {
      res.sendStatus(400);
    }
  });

  router.post("/create", async (req, res) => {
    if (tunneler.isActive()) {
      res.status(400).json({ url: tunneler.url });
    } else {
      try {
        let url = await tunneler.createTunnel(port);
        socketHandler.start(server);
        bearer = generateBearer();
        res.status(200).json({ url, bearer });
      } catch (err) {
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

  router.get("/socketTunnel", (req, res) => {
    if (tunneler.isActive()) res.status(200).send(tunneler.url);
    else res.sendStatus(400);
  });

  return router;
};
