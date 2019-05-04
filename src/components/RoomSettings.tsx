import React, { Component } from "react";
import { Button } from "../components";

interface Props {
  setMedia: () => void;
  closeRoom: () => void;
}

class RoomSettings extends Component<Props> {
  constructor(props: Props) {
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
