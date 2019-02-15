import React, { Component } from "react";
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import { Home, Room } from "../pages";

class Navigator extends Component {
  render() {
    return (
      <div className="full">
        <Router>
          <Switch>
            <Route path="/" exact component={Home} />
            <Route path="/room/:id" exact component={Room} />
          </Switch>
        </Router>
      </div>
    );
  }
}

export default Navigator;
