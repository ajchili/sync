import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { RoomSidebar } from "../components";
import { Communicator, Swal } from "../services";
import { SocketCommunicator, SocketUser, UserMessage } from "../lib";
import io from "socket.io-client";

interface State {
  url: string | null;
  users: Array<SocketUser>;
  messages: Array<UserMessage>;
}

class Room extends Component<any, State> {
  socket?: SocketCommunicator;
  videoRef: React.RefObject<HTMLVideoElement>;
  constructor(props: any) {
    super(props);
    this.videoRef = React.createRef();
    this.state = {
      url: null,
      users: [],
      messages: []
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
    try {
      let socketURL: string = await Communicator.getSocketURL(match.params.id);
      this.socket = new SocketCommunicator(io(socketURL), isHost);
      this.socket.on("media", (url: string) => {
        this.setState({ url });
        let video: HTMLVideoElement | null = this.videoRef.current;
        if (video) this._setupVideoElement(video);
      });
      this.socket.on("play", () => {
        let video: HTMLVideoElement | null = this.videoRef.current;
        if (video) video.play();
      });
      this.socket.on("pause", () => {
        let video: HTMLVideoElement | null = this.videoRef.current;
        if (video) video.pause();
      });
      this.socket.on(
        "mediaTime",
        (data: { time: number; state: "playing" | "paused" }) => {
          let video: HTMLVideoElement | null = this.videoRef.current;
          if (video) {
            let timeDifference = Math.abs(video.currentTime - data.time);
            if (
              (isHost && timeDifference > 1 && !video.seeking) ||
              timeDifference > 2
            ) {
              video.currentTime = data.time;
            }
            if (!isHost) {
              if (data.state === "playing" && video.paused) {
                video.play();
              } else if (data.state === "paused" && !video.paused) {
                video.pause();
              }
            }
          }
        }
      );
      this.socket.on("users", (users: Array<SocketUser>) => {
        this.setState({ users });
      });
      this.socket.on("message", (message: UserMessage) => {
        const { messages } = this.state;
        messages.push(message);
        this.setState({ messages });
      });
      this.socket.on("close", () => {
        history.push(
          "/",
          location.state && location.state.host && { roomClosed: true }
        );
      });
    } catch (err) {
      console.error(err);
      history.push("/", { roomDoesNotExist: true });
    }
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
    const { location, match } = this.props;
    const { url, users, messages } = this.state;

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
              width: this.socket ? "80%" : "100%",
              height: "100%",
              backgroundColor: "#000000",
              float: "left"
            }}
          />
        )}
        {this.socket && (
          <RoomSidebar
            roomId={match.params.id}
            isHost={location.state && location.state.host}
            setMedia={this._setMedia}
            closeRoom={this._closeRoom}
            sendMessage={(message: string) => this.socket!.sendMessage(message)}
            users={users}
            messages={messages}
          />
        )}
      </div>
    );
  }
}

export default withRouter(Room);
