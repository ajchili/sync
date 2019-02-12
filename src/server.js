// @ts-ignore
const express = require("express");
const app = express();
const port = process.env.PORT || 8080;

app.get("/", (req, res) => res.send("Hello World!"));
// eslint-disable-next-line
app.listen(port, () => console.log(`Sync listening on port ${port}!`));
