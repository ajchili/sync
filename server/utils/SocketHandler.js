const SocketUser = require("./SocketUser");

class SocketHanlder {
  constructor() {
    this.io = null;
    this.bearer = null;
    this.media = null;
    this.mediaState = "paused";
    this.mediaTime = 0;
    this.connectedUsers = [];
    this.messages = [];
    this.emitConnectedUsersInterval = null;
  }

  setMedia(media) {
    this.media = media;
    this.mediaState = "paused";
    this.mediaTime = 0;
    if (this.io) this.io.emit("media", this.media);
  }

  start(server, bearer) {
    this.io = require("socket.io")(server, {
      pingInterval: 1000
    });
    this.bearer = bearer;
    this.setupEventHanlders();
    this.emitConnectedUsersInterval = setInterval(() => {
      this.emitConnectedUsers();
    }, 500);
  }

  stop() {
    this.io.emit("closed", {});
    this.io = null;
    this.bearer = null;
    this.media = null;
    this.mediaState = "paused";
    this.mediaTime = 0;
    this.connectedUsers = [];
    this.messages = [];
    clearInterval(this.emitConnectedUsersInterval);
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
      let user = new SocketUser(socket);
      this.connectedUsers.push(user);
      this.emitConnectedUsers();
      if (this.media) {
        socket.emit("media", this.media);
        socket.emit("mediaTime", {
          time: this.mediaTime,
          state: this.mediaState
        });
      }
      socket.on("authenticate", data => {
        user.isHost = data.bearer === this.bearer;
        this.emitConnectedUsers();
        this.emitMessages();
      });
      socket.on("message", data => {
        let id = `_${Math.random()
          .toString(36)
          .substr(2, 9)}`;
        let sender = user.displayName;
        let timeSent = new Date().getTime();
        let message = { id, sender, body: data.message, timeSent };
        this.messages.push(message);
        if (this.io) this.io.emit("message", { message });
      });
      socket.on("disconnect", () => {
        this.connectedUsers = this.connectedUsers.filter(connectedUser => {
          return connectedUser.socket.id !== socket.id;
        });
        this.emitConnectedUsers();
      });
    });
  }

  emitConnectedUsers() {
    let users = this.connectedUsers.map(user => {
      return {
        id: user.socket.id,
        displayName: user.displayName || user.socket.id,
        ping: user.ping,
        isHost: user.isHost
      };
    });
    if (this.io) this.io.emit("users", { users });
  }

  emitMessages() {
    if (this.io) this.io.emit("messages", { messages: this.messages });
  }
}

module.exports = SocketHanlder;
