const ngrok = require("ngrok");

class Tunneler {
  constructor() {
    this.constructing = false;
    this.url = null;
  }

  isActive() {
    return this.url;
  }

  createTunnel(port = 8080) {
    return new Promise((resolve, reject) => {
      if (this.isActive()) reject(Error("Tunnel already exists!"));
      else if (this.constructing)
        reject(Error("Tunnel creation already in progress!"));
      else this.constructing = true;
      ngrok
        .connect(port)
        .then(url => {
          this.url = url;
          resolve(url);
        })
        .catch(err => {
          reject(err);
        })
        .finally(() => {
          this.constructing = false;
        });
    });
  }

  closeTunnel() {
    return new Promise((resolve, reject) => {
      if (!this.isActive()) reject(new Error("Tunnel does not exist!"));
      else {
        ngrok
          .disconnect()
          .then(resolve)
          .catch(err => reject(err))
          .finally(() => {
            this.url = null;
          });
      }
    });
  }
}

module.exports = Tunneler;
