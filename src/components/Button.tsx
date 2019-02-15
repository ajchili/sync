import React, { Component } from "react";

class Button extends Component<any, any> {
  constructor(props: {
    title: string;
    onClick: () => {};
    disabled: boolean;
    style: any;
  }) {
    super(props);
    this.state = {
      clicked: false
    };
  }

  render() {
    const { title, onClick, style } = this.props;
    const disabled = this.props.disabled || false;
    return (
      <div
        className={`button unselectable ${disabled && "disabled"}`}
        style={style}
        onClick={onClick}
      >
        {title}
      </div>
    );
  }
}

export default Button;
