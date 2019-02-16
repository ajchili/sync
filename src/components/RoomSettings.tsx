import React, { Component } from "react";
import { Button } from ".";

class RoomSettings extends Component<any, any> {
  constructor(props: { setMedia: () => {}; closeRoom: () => {} }) {
    super(props);
  }

  render() {
    const { setMedia, closeRoom } = this.props;

    return (
      <div>
        <h4 className="light">Settings</h4>
        <Button
          title="Set Media"
          onClick={setMedia}
          light
          style={{ marginRight: "1em" }}
        />
        <Button title="Close Room" onClick={closeRoom} light />
      </div>
    );
  }
}

export default RoomSettings;
