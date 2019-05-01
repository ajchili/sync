class SocketHanlder {
  constructor() {
    this.io = null;
    this.media = null;
  }

  setMedia(media) {
    this.media = media;
    if (this.io) this.io.emit("media", this.media);
  }

  start(server) {
    this.io = require("socket.io")(server);
    this.setupEventHanlders();
  }

  stop() {
    this.io.emit("closed", {});
    this.io = null;
    this.media = null;
  }

  setupEventHanlders() {
    this.io.on("connection", socket => {
      if (this.media) socket.emit("media", this.media);
    });
  }
}

module.exports = SocketHanlder;
