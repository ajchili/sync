import React, { Component } from "react";
import { RoomSettings, RoomViewers } from "../components";

interface User {
  id: string;
  displayName: string;
  ping: number;
  isHost: boolean;
}

interface Props {
  roomId: string;
  isHost: boolean;
  setMedia: () => void;
  closeRoom: () => void;
  users: Array<User>;
}

class RoomSizebar extends Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  render() {
    const { roomId, isHost, setMedia, closeRoom, users } = this.props;

    return (
      <div
        style={{
          width: "20%",
          height: "100%",
          backgroundColor: "#000000",
          float: "left",
          display: "flex",
          flexFlow: "column"
        }}
      >
        <h4 className="light">Room ID: {roomId}</h4>
        {isHost && <RoomSettings setMedia={setMedia} closeRoom={closeRoom} />}
        <h4 className="light">Viewers</h4>
        <RoomViewers users={users} />
        <h4 className="light">Chat</h4>
        <div
          style={{
            width: "100%",
            overflowY: "scroll",
            backgroundColor: "#000000",
            flexGrow: 1,
            display: "flex",
            flexFlow: "column"
          }}
        />
      </div>
    );
  }
}

export default RoomSizebar;
