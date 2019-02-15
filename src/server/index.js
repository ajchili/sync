const bodyParser = require("body-parser");
const express = require("express");
const app = express();
const port = process.env.PORT || 8080;

app.use(bodyParser.urlencoded({ extended: true }));
// cors
app.use((req, res, next) => {
  if (req.headers.origin !== "http://localhost:3000") {
    return res.status(401).send();
  }
  res.header("Access-Control-Allow-Origin", "*");
  res.header(
    "Access-Control-Allo-Headers",
    "Origin, X-Requested-With, Content-Type, Accept"
  );
  next();
});
app.use("/", require("./routes"));

app.listen(port, () => console.log(`Sync listening on port ${port}!`));
