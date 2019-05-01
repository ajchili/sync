class SocketHanlder {
  constructor() {
    this.io = null;
    this.media = null;
    this.mediaState = "paused";
    this.mediaTime = 0;
  }

  setMedia(media) {
    this.media = media;
    this.mediaState = "paused";
    this.mediaTime = 0;
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
    this.mediaState = "paused";
    this.mediaTime = 0;
  }

  play() {
    if (this.media) {
      this.mediaState = "playing";
      this.io.emit("play", {});
    }
  }

  pause() {
    if (this.media) {
      this.mediaState = "paused";
      this.io.emit("pause", {});
    }
  }

  seek(time) {
    if (this.media) {
      this.mediaTime = time;
      this.io.emit("mediaTime", {
        time,
        state: this.mediaState
      });
    }
  }

  setupEventHanlders() {
    this.io.on("connection", socket => {
      if (this.media) socket.emit("media", this.media);
    });
  }
}

module.exports = SocketHanlder;
