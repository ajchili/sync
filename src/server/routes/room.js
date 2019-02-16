const express = require("express");
const router = express.Router();
const SocketHandler = require("../utils/SocketHandler");
const Tunneler = require("../utils/Tunneler");

module.exports = (http, port = 8080, socketPort = 8081) => {
  let tunneler = new Tunneler();
  let socketTunneler = new Tunneler();
  let socketHandler = new SocketHandler();

  router.post("/close", (req, res) => {
    if (tunneler.isActive()) {
      socketHandler.stop();
      tunneler.closeTunnel();
      setTimeout(() => {
        socketTunneler.closeTunnel();
        res.sendStatus(200);
      }, 1500);
    } else {
      res.sendStatus(400);
    }
  });

  router.post("/create", async (req, res) => {
    if (tunneler.isActive()) {
      res.status(400).json(tunneler.tunnel);
    } else {
      try {
        let tunnel = await tunneler.createTunnel(port);
        await socketTunneler.createTunnel(socketPort);
        socketHandler.start(http);
        res.status(200).json(tunnel);
      } catch (err) {
        res.status(500).json(err);
      }
    }
  });

  router.get("/socketTunnel", (req, res) => {
    if (socketTunneler.isActive()) {
      res.status(200).send(socketTunneler.tunnel);
    } else {
      res.sendStatus(400);
    }
  });

  return router;
};
