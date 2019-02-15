import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { Button } from "../components";

class ErrorPage404 extends Component<any, any> {
  constructor(props: {}) {
    super(props);
  }

  _goHome = () => {
    const { history } = this.props;
    history.replace("/");
  }

  render() {
    return (
      <div className="centered">
        <h1>404 - Page not found</h1>
        <p>It looks like you are lost</p>
        <div>
          <Button title="Go Home" onClick={this._goHome}/>
        </div>
      </div>
    );
  }
}

export default withRouter(ErrorPage404);
