import React, { Component } from "react";
import { RoomSettings, RoomViewers } from "../components";

export interface User {
  id: string;
  displayName: string;
  ping: number;
  isHost: boolean;
}

export interface Message {
  id: string;
  sender: string;
  body: string;
  timeSent: number;
}

interface Props {
  roomId: string;
  isHost: boolean;
  setMedia: () => void;
  closeRoom: () => void;
  sendMessage: (message: string) => void;
  users: Array<User>;
  messages: Array<Message>;
}

interface State {
  message: string;
  isSendingMessage: boolean;
}

class RoomSizebar extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      message: "",
      isSendingMessage: false
    };
  }

  render() {
    const {
      roomId,
      isHost,
      setMedia,
      closeRoom,
      sendMessage,
      users,
      messages
    } = this.props;
    const { message, isSendingMessage } = this.state;

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
            backgroundColor: "#000000",
            flexGrow: 1,
            display: "flex",
            flexFlow: "column"
          }}
        >
          <div
            style={{
              flexGrow: 1,
              backgroundColor: "#000000",
              overflowY: "scroll",
              display: "flex",
              flexFlow: "column",
              maxHeight: "100%"
            }}
          >
            {messages.map((message: Message) => {
              return (
                <p key={message.id} style={{ color: "#ffffff", margin: 0 }}>
                  {message.sender}: {message.body}
                </p>
              );
            })}
          </div>

          <input
            type="text"
            placeholder="Send a message..."
            style={{
              backgroundColor: "#000000",
              color: isSendingMessage ? "grey" : "#ffffff",
              border: 0,
              padding: 7.5,
              fontSize: 16,
              display: "flex"
            }}
            disabled={isSendingMessage}
            value={message}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
              this.setState({ message: e.target.value })
            }
            onKeyDown={async (e: React.KeyboardEvent<HTMLInputElement>) => {
              let trimmedMessage = message.trim();
              if (e.key === "Enter" && trimmedMessage.length) {
                this.setState({ isSendingMessage: true });
                sendMessage(message);
                setTimeout(() => {
                  this.setState({ message: "", isSendingMessage: false });
                }, 250);
              }
            }}
          />
        </div>
      </div>
    );
  }
}

export default RoomSizebar;
