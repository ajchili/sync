import React, { Component } from "react";
import { Button, Tooltip } from "../components";
import { Communicator } from "../services";

class Home extends Component<any, any> {
  constructor(props: {}) {
    super(props);
    this.state = {
      web: false
    };
  }

  async componentDidMount() {
    try {
      let type = await Communicator.getClientType();
      this.setState({ web: type === "web" });
    } catch (err) {
      this.setState({ web: true });
    }
  }

  async _createRoom() {
    try {
      let tunnel = await Communicator.createRoom();
    } catch (err) {
      switch (err.response.status) {
        case 400:
          // Room already exists
          break;
        case 500:
          // Error creating room
          break;
        default:
          // Unexpected error
          break;
      }
    }
  }

  render() {
    const { web } = this.state;
    return (
      <div className="centered">
        <h1>sync</h1>
        <p>The simplest way to watch media together</p>
        <div>
          <Button title={"Join"} style={{ marginRight: "1em" }} />
          {web ? (
            <Tooltip
              tooltip={"You must be using the sync client to host a room."}
              component={<Button title={"Host"} disabled />}
            />
          ) : (
            <Button title={"Host"} onClick={this._createRoom} />
          )}
        </div>
      </div>
    );
  }
}

export default Home;
