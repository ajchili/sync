import { EventEmitter } from "events";
import { Communicator } from "../services";

export default class SocketCommunicator extends EventEmitter {
  private _socket: SocketIOClient.Socket;
  private _isHost: boolean;
  constructor(socket: SocketIOClient.Socket, isHost?: boolean) {
    super();
    this._socket = socket;
    this._isHost = isHost || false;
    this.setupSocket();
    if (isHost) this.emitAuthenticationMessage();
    this.emitDisplayName();
  }
  get socket() {
    return this._socket;
  }
  private setupSocket() {
    this._socket.on("pong", (ping: number) => {
      this._socket.emit("latency", { ping });
    });
    this._socket.on("media", (url: string) => {
      this.emit("media", url);
    });
    this._socket.on("play", () => {
      if (!this._isHost) this.emit("play");
    });
    this._socket.on("pause", () => {
      if (!this._isHost) this.emit("pause");
    });
    this._socket.on("mediaTime", (data: any) => {
      this.emit("mediaTime", data);
    });
    this._socket.on("users", (data: { users: Array<any> }) => {
      this.emit("users", data.users);
    });
    this._socket.on("closed", () => {
      this.emit("closed");
    });
  }
  private emitAuthenticationMessage() {
    this._socket.emit("authenticate", { bearer: Communicator.Bearer() });
  }
  private emitDisplayName() {
    let displayName: string | null = window.localStorage.getItem("displayName");
    if (!!displayName) this._socket.emit("displayName", { displayName });
  }
  disconnect() {
    this._socket.disconnect();
  }
}
