import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { Button } from "../components";
import { Communicator } from "../services";

class Room extends Component<any, any> {
  constructor(props: {}) {
    super(props);
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
  }

  render() {
    const { match } = this.props;

    return (
      <div className="centered">
        {match.params.id}
        <br />
        <br />
        <Button title="Close" onClick={this._closeRoom} />
      </div>
    );
  }
}

export default withRouter(Room);
