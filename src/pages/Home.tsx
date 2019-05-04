import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { Button, Tooltip } from "../components";
import { Communicator, Swal } from "../services";

class Home extends Component<any, any> {
  constructor(props: {}) {
    super(props);
    this.state = {
      web: false
    };
  }

  async componentDidMount() {
    const { history, location } = this.props;
    if (location.state) {
      if (location.state.roomClosed) {
        Swal.showAlert("Room Closed");
      } else if (location.state.roomDoesNotExist) {
        Swal.showError("Unable to Join Room", "Room does not exist!");
      }
      history.push("/");
    }
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
      Swal.showLoading();
      let id = await Communicator.createRoom();
      history.push(`/room/${id}`, { host: true });
      Swal.hide();
    } catch (err) {
      console.error(err);
      Swal.showError("Unable to Create Room");
    }
  };

  _joinRoom = async () => {
    const { history } = this.props;
    let id: string | null = await Swal.showInput("Enter Room ID");
    if (id) {
      try {
        Swal.showLoading();
        await Communicator.getSocketURL(id);
        history.push(`/room/${id}`, { host: false });
        Swal.hide();
      } catch (err) {
        console.error(err);
        Swal.showError(
          "Unable to Join Room",
          "Please ensure the room id specified is correct"
        );
      }
    }
  };

  _setDisplayName = async () => {
    let displayName: string | null = await Swal.showInput("New Display Name");
    if (displayName && displayName.trim().length) {
      window.localStorage.setItem("displayName", displayName.trim());
    }
  };

  render() {
    const { web } = this.state;
    return (
      <div className="full">
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
        <Button
          title="Set Display Name"
          style={{ position: "absolute", bottom: 10, right: 10 }}
          onClick={this._setDisplayName}
        />
      </div>
    );
  }
}

export default withRouter(Home);
