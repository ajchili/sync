const bodyParser = require("body-parser");
const cors = require("cors");
const express = require("express");
const app = express();
const http = require("http").Server(app);
const port = process.env.PORT || 8080;
const socketPort = port + 1;

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(cors());
app.use("/", require("./routes"));
app.use("/room", require("./routes/room")(http, port, socketPort));

app.listen(port, () => {
  console.log(`Sync listening on port ${port}!`);
  http.listen(socketPort);
  console.log(`Sync listening on port ${socketPort}!`);
});
