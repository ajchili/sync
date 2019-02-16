import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { RoomSidebar } from "../components";
import { Communicator, Swal } from "../services";
// @ts-ignore
import io from "socket.io-client";

class Room extends Component<any, any> {
  constructor(props: {}) {
    super(props);
    this.state = {
      url: null
    };
  }

  async componentDidMount() {
    const { history, location, match } = this.props;
    try {
      let socketURL = await Communicator.getSocketURL(match.params.id);
      const socket = io(socketURL);
      socket.on("media", (data: any) => {
        this.setState({ url: data });
      });
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
      Swal.showLoading("Closing Server");
      await Communicator.closeRoom();
    } catch (err) {
      // Do nothing if this fails.
      // It will only fail if no tunnel is active.
      // This means that the server was not running to begin with.
    } finally {
      Swal.hide();
      history.replace("/");
    }
  };

  _setMedia = async () => {
    try {
      let mediaType = await Swal.showChoice({
        title: "Select Media Type",
        confirmButtonText: "Online Media"
      });
      switch (mediaType) {
        case 0:
          let url = await Swal.showURLInput("Media URL");
          if (url) await Communicator.setMedia({ url });
          break;
      }
    } catch (err) {}
  };

  render() {
    const { url } = this.state;
    const { location, match } = this.props;

    return (
      <div className="full noScroll">
        {url ? (
          <video
            style={{
              width: "80%",
              height: "100%",
              backgroundColor: "#000000",
              float: "left"
            }}
            controls={true}
            src={url}
          />
        ) : (
          <div
            style={{
              width: "80%",
              height: "100%",
              backgroundColor: "#000000",
              float: "left"
            }}
          />
        )}
        <RoomSidebar
          roomId={match.params.id}
          isHost={location.state && location.state.host}
          setMedia={this._setMedia}
          closeRoom={this._closeRoom}
        />
      </div>
    );
  }
}

export default withRouter(Room);
