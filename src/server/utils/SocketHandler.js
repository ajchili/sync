class SocketHanlder {
  constructor() {
    this.io = null;
  }

  start(http) {
    this.io = require("socket.io")(http);
    this.setupEventHanlders();
  }

  stop() {
    this.io.close();
    this.io = null;
  }

  setupEventHanlders() {
    this.io.on("connection", socket => {
      console.log("New connection:", socket);
    });
  }
}

module.exports = SocketHanlder;
