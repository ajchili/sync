import React, { Component } from "react";

class RoomSizebar extends Component<any, any> {
  constructor(props: {}) {
    super(props);
  }

  render() {
    return (
      <div
        style={{
          width: "100%",
          maxHeight: "15%",
          backgroundColor: "#000000",
          overflowY: "scroll"
        }}
      />
    );
  }
}

export default RoomSizebar;
