class SocketUser {
  constructor(socket) {
    this.socket = socket;
    this.displayName = null;
    this.ping = -1;
    this.isHost = false;
    socket.on("latency", data => {
      this.ping = data.ping;
    });
    socket.on("displayName", data => {
      this.displayName = data.displayName;
    });
  }
}

module.exports = SocketUser;
