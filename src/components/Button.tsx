import React, { Component } from "react";

interface Props {
  title: string;
  onClick?: () => void;
  disabled?: any;
  light?: any;
  style?: any;  
}

interface State {
  clicked: boolean;
}

class Button extends Component<Props, State> {
  constructor(props: Props) {
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
