const express = require("express");
const router = express.Router();
const Tunneler = require("../utils/Tunneler");

let tunneler = new Tunneler();

router.post("/close", (req, res) => {
  if (tunneler.tunnel) {
    tunneler.closeTunnel();
    res.sendStatus(200);
  } else {
    res.sendStatus(400);
  }
});

router.post("/create", async (req, res) => {
  if (tunneler.isActive()) {
    res.status(400).json(tunneler.tunnel);
  } else {
    try {
      let tunnel = await tunneler.createTunnel();
      res.status(200).json(tunnel);
    } catch (err) {
      res.status(500).json(err);
    }
  }
});

module.exports = router;
