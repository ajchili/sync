import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { RoomSidebar } from "../components";
import { Communicator, Swal } from "../services";
//@ts-ignore
import io from "socket.io-client";

interface State {
  url: string | null;
}

class Room extends Component<any, State> {
  socket: any;
  videoRef: React.RefObject<HTMLVideoElement>;
  constructor(props: {}) {
    super(props);
    this.videoRef = React.createRef();
    this.state = {
      url: null
    };
  }

  componentDidMount() {
    this._setupSocketListener();
  }

  componentWillUnmount() {
    if (this.socket) this.socket.disconnect();
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
          };
          input.click();
          break;
      }
    } catch (err) {}
  };

  _setupSocketListener = async () => {
    const { history, location, match } = this.props;
    let isHost = location.state && location.state.host;
    let socketURL;
    try {
      socketURL = await Communicator.getSocketURL(match.params.id);
    } catch (err) {
      console.error(err);
      history.push("/", { roomDoesNotExist: true });
    }
    this.socket = io(socketURL);
    this.socket.on("media", (data: string) => {
      this.setState({ url: data });
      let video: HTMLVideoElement | null = this.videoRef.current;
      if (video) this._setupVideoElement(video);
    });
    this.socket.on("play", () => {
      let video: HTMLVideoElement | null = this.videoRef.current;
      if (!isHost && video) video.play();
    });
    this.socket.on("pause", () => {
      let video: HTMLVideoElement | null = this.videoRef.current;
      if (!isHost && video) video.pause();
    });
    this.socket.on(
      "mediaTime",
      (data: { time: number; status: "playing" | "paused" }) => {
        let video: HTMLVideoElement | null = this.videoRef.current;
        if (!isHost && video) {
          switch (data.status) {
            case "playing":
              if (video.paused) video.play();
              break;
            case "paused":
              if (!video.paused) video.pause();
              break;
          }
          let timeDifference = data.time - video.currentTime;
          // If there is a time difference of more than 2 seconds
          // skip user to time.
          if (Math.abs(timeDifference) > 2) {
            video.currentTime = data.time;
          }
        }
      }
    );
    this.socket.on("closed", () => {
      history.push(
        "/",
        location.state && location.state.host && { roomClosed: true }
      );
    });
  };

  _setupVideoElement = (video: HTMLVideoElement) => {
    const { location } = this.props;
    let isHost = location.state && location.state.host;
    if (!isHost) return;
    video.onplay = async () => {
      try {
        await Communicator.playMedia();
      } catch (err) {
        console.error("Unable to trigger play event:", err);
      }
    };
    video.onpause = async () => {
      try {
        await Communicator.pauseMedia();
      } catch (err) {
        console.error("Unable to trigger pause event:", err);
      }
    };
    const timeUpdated = async () => {
      try {
        await Communicator.setMediaTime(video.currentTime);
      } catch (err) {
        console.error("Unable to trigger seek event:", err);
      }
    };
    video.onseeking = timeUpdated;
    video.ontimeupdate = timeUpdated;
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
