import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { RoomSidebar } from "../components";
import { Communicator, Swal } from "../services";
//@ts-ignore
import io from "socket.io-client";

let socket: any;

interface State {
  url: string | null;
}

class Room extends Component<any, State> {
  videoRef: React.RefObject<HTMLVideoElement>;
  constructor(props: {}) {
    super(props);
    this.videoRef = React.createRef();
    this.state = {
      url: null
    };
  }

  async componentDidMount() {
    const { history, location, match } = this.props;
    try {
      let socketURL = await Communicator.getSocketURL(match.params.id);
      socket = io(socketURL);
      socket.on("media", (data: string) => {
        this.setState({ url: data });
      });
      socket.on("closed", () => {
        history.push(
          "/",
          location.state && location.state.host && { roomClosed: true }
        );
      });
    } catch (err) {
      console.error(err);
      history.push("/", { roomDoesNotExist: true });
    }
  }

  componentWillUnmount() {
    if (socket) socket.disconnect();
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
        confirmButtonText: "Online Media",
        cancelButtonText: "Local Media"
      });
      switch (mediaType) {
        case 0:
          let url = await Swal.showURLInput("Media URL");
          if (url) await Communicator.setMedia({ url });
          break;
        case 1:
          let input = document.createElement("input");
          input.type = "file";
          input.accept = "video/*";
          input.onchange = async e => {
            //@ts-ignore
            let file: File = e.target.files[0];
            if (file) await Communicator.setMedia({ file });
          }
          input.click();
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
            ref={this.videoRef}
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
