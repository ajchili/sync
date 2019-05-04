const express = require("express");
const router = express.Router();

router.get("/", (req, res) => {
  res.send("Your sync server is running.");
});

module.exports = router;
