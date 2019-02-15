import React, { Component } from "react";
import { withRouter } from "react-router-dom";
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
    const { history } = this.props;
    try {
      let id = await Communicator.createRoom();
      history.push(`/room/${id}`);
    } catch (err) {
      switch (err.response.status) {
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
            <Button title={"Host"} onClick={this._createRoom.bind(this)} />
          )}
        </div>
      </div>
    );
  }
}

export default withRouter(Home);
