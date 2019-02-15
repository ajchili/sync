const bodyParser = require("body-parser");
const express = require("express");
const app = express();
const http = require("http").Server(app);
const port = process.env.PORT || 8080;
const socketPort = port + 1;

app.use(bodyParser.urlencoded({ extended: true }));
app.use((req, res, next) => {
  res.header("Access-Control-Allow-Origin", "*");
  res.header(
    "Access-Control-Allow-Headers",
    "Origin, X-Requested-With, Content-Type, Accept"
  );
  next();
});
app.use("/", require("./routes"));
app.use("/room", require("./routes/room")(http, port, socketPort));

app.listen(port, () => {
  console.log(`Sync listening on port ${port}!`);
  http.listen(socketPort);
  console.log(`Sync listening on port ${socketPort}!`);
});
