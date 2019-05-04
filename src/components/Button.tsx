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
    const disabled = (this.props.disabled && "disabled") || "";
    const light = (this.props.light && "light") || "";
    const classes = `button unselectable ${disabled} ${light}`;
    return (
      <div className={classes} style={style} onClick={onClick}>
        {title}
      </div>
    );
  }
}

export default Button;
