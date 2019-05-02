import React, { Component } from "react";

interface User {
  id: string;
  displayName: string;
  ping: number;
  isHost: boolean;
}

interface Props {
  users: Array<User>;
}

class RoomSizebar extends Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  render() {
    const { users } = this.props;

    return (
      <div
        style={{
          width: "100%",
          maxHeight: "15%",
          backgroundColor: "#000000",
          overflowY: "scroll"
        }}
      >
        {users.map((user: User) => {
          return (
            <p key={user.id} style={{ color: user.isHost ? "#4caf50" : "#FFFFFF" }}>
              {user.displayName} ({user.ping})
            </p>
          );
        })}
      </div>
    );
  }
}

export default RoomSizebar;
