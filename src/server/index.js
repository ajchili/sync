const bodyParser = require("body-parser");
const cors = require("cors");
const express = require("express");
const app = express();
const server = require("http").createServer(app);
const port = process.env.PORT || 8080;

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(cors());
app.use("/", require("./routes"));
app.use("/room", require("./routes/room")(server, port));

app.start = app.listen = function() {
  console.log(`Sync listening on port ${port}!`);
  return server.listen.apply(server, arguments);
};

app.start(port);
