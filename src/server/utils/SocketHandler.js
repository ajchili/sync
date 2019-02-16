class SocketHanlder {
  constructor() {
    this.io = null;
  }

  start(http) {
    this.io = require("socket.io")(http);
    this.setupEventHanlders();
  }

  stop() {
    this.io.emit("closed", {});
    this.io.close();
    this.io = null;
  }

  setupEventHanlders() {
    this.io.on("connection", socket => {
      console.log("New connection");
    });
  }
}

module.exports = SocketHanlder;
