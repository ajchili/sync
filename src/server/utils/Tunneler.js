const localtunnel = require("localtunnel");

class Tunneler {
  constructor() {
    this.tunnel = null;
    this.creatingTunnel = false;
  }

  isActive() {
    return this.tunnel && !this.tunnel._closed;
  }

  createTunnel(port = 8080) {
    return new Promise((resolve, reject) => {
      if (this.isActive()) reject(Error("Tunnel already exists!"));
      else if (this.creatingTunnel === true)
        reject(Error("Tunnel creation already in progress!"));
      else this.creatingTunnel = true;
      localtunnel(port, (err, tunnel) => {
        this.creatingTunnel = false;
        if (err) reject(err);
        else {
          this.tunnel = tunnel;
          resolve(this.tunnel);
        }
      });
    });
  }

  closeTunnel() {
    if (!this.tunnel) throw new Error("Tunnel does not exist!");
    else this.tunnel.close();
  }
}

module.exports = Tunneler;
