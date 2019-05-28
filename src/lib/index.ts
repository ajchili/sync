import SocketCommunicator from "./SocketCommunicator";

export interface SocketUser {
  id: string;
  displayName: string;
  ping: number;
  isHost: boolean;
}

export interface UserMessage {
  id: string;
  sender: string;
  body: string;
  timeSent: number;
}


export { SocketCommunicator };
