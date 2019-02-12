import React, { Component } from "react";
import { Button } from "../components";

const isInBrowser = !!process.env.BROWSER || true;

class Home extends Component<any, any> {
  render() {
    return (
      <div className="centered">
        <h1>sync</h1>
        <p>The simplest way to watch media together</p>
        <div>
          <Button title={"Join"} style={{ marginRight: "1em" }} />
          {isInBrowser ? (
            <Button
              title={"Host"}
              tooltip={"You must be using the sync client to host a room."}
              disabled
            />
          ) : (
            <Button title={"Host"} />
          )}
        </div>
      </div>
    );
  }
}

export default Home;
