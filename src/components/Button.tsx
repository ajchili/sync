import React, { Component } from "react";

class Button extends Component<any, any> {
  constructor(props: {
    title: string;
    onClick: () => {};
    disabled: boolean;
    tooltip: string;
    style: any;
  }) {
    super(props);
    this.state = {
      clicked: false
    };
  }

  render() {
    const { title, onClick, tooltip, style } = this.props;
    const disabled = this.props.disabled || false;
    return (
      <div
        className={`button unselectable ${disabled && "disabled"} ${!!tooltip &&
          "tooltip"}`}
        style={style}
        onClick={onClick}
      >
        {title}
        <span className="tooltiptext">{tooltip || ""}</span>
      </div>
    );
  }
}

export default Button;
