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

  _createRoom = async () => {
    const { history } = this.props;
    try {
      let id = await Communicator.createRoom();
      history.push(`/room/${id}`, { host: true });
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
  };

  _joinRoom = async () => {
    const { history } = this.props;
    let id: string | null = prompt("Enter Room ID");
    if (id) {
      try {
        await Communicator.getSocketURL(id);
        history.push(`/room/${id}`, { host: false });
      } catch (err) {
        switch (err.response.status) {
          default:
            // Unexpected error
            break;
        }
      }
    }
  };

  render() {
    const { web } = this.state;
    return (
      <div className="centered">
        <h1>sync</h1>
        <p>The simplest way to watch media together</p>
        <div>
          <Button
            title={"Join"}
            style={{ marginRight: "1em" }}
            onClick={this._joinRoom}
          />
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

export default withRouter(Home);
