// @ts-ignore
const express = require("express");
const app = express();
const port = process.env.PORT || 8080;

app.use("/", require("./routes"));

// eslint-disable-next-line
app.listen(port, () => console.log(`Sync listening on port ${port}!`));
