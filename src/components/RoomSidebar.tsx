import React, { Component } from "react";
import { RoomSettings, RoomViewers } from ".";

class RoomSizebar extends Component<any, any> {
  constructor(props: { roomId: string; isHost: boolean; closeRoom: () => {} }) {
    super(props);
  }

  render() {
    const { roomId, isHost, closeRoom } = this.props;

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
        {isHost && <RoomSettings roomId={roomId} closeRoom={closeRoom} />}
        <h4 className="light">Viewers</h4>
        <RoomViewers />
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
