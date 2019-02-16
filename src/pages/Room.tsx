import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { Button } from "../components";
import { Communicator } from "../services";
// @ts-ignore
import io from "socket.io-client";

class Room extends Component<any, any> {
  constructor(props: {}) {
    super(props);
  }

  async componentDidMount() {
    const { history, location, match } = this.props;
    try {
      let socketURL = await Communicator.getSocketURL(match.params.id);
      const socket = io(socketURL);
      socket.on("closed", () => {
        history.push(
          "/",
          location.state && location.state.host && { roomClosed: true }
        );
      });
    } catch (err) {
      history.push("/", { roomDoesNotExist: true });
    }
  }

  _closeRoom = async () => {
    const { history } = this.props;
    try {
      await Communicator.closeRoom();
    } catch (err) {
      // Do nothing if this fails.
      // It will only fail if no tunnel is active.
      // This means that the server was not running to begin with.
    } finally {
      history.replace("/");
    }
  };

  render() {
    const { location } = this.props;

    return (
      <div className="full noScroll">
        <video
          style={{
            width: "75%",
            height: "100%",
            backgroundColor: "#000000",
            float: "left"
          }}
          controls={true}
        />
        <div
          style={{
            width: "25%",
            height: "100%",
            backgroundColor: "#000000",
            float: "left"
          }}
        >
          {location.state && location.state.host && (
            <Button
              title="Close Room"
              onClick={this._closeRoom}
              light
              style={{
                position: "absolute",
                top: 10,
                right: 10
              }}
            />
          )}
        </div>
      </div>
    );
  }
}

export default withRouter(Room);
