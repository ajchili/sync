// @ts-ignore
const localtunnel = require("localtunnel");
const EventEmitter = require("events");

class Tunneler extends EventEmitter {
  constructor() {
    super();
    this.tunnel = null;
    this.creatingTunnel = false;
  }

  createTunnel(port = 8080) {
    if (this.tunnel) throw new Error("Tunnel already exists!");
    else if (this.creatingTunnel === true)
      throw new Error("Tunnel creation already in progress!");
    else this.creatingTunnel = true;
    localtunnel(port, (err, tunnel) => {
      this.creatingTunnel = false;
      if (err) throw err;
      else {
        this.tunnel = tunnel;
        this.emit("created", this.tunnel);
        this.tunnel.on("close", () => {
          this.emit("closed");
          this.tunnel = null;
        });
      }
    });
  }

  closeTunnel() {
    if (!this.tunnel) throw new Error("Tunnel does not exist!");
    else this.tunnel.close();
  }
}

module.exports = Tunneler;
