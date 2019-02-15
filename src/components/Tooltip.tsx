import React, { Component } from "react";

class Tooltip extends Component<any, any> {
  constructor(props: { tooltip: string; component: Component }) {
    super(props);
  }

  render() {
    const { tooltip, component, ...props } = this.props;
    return (
      <div className="tooltip" {...props}>
        {component}
        <span className="tooltiptext">{tooltip || ""}</span>
      </div>
    );
  }
}

export default Tooltip;
