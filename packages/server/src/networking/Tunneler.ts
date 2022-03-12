import * as ngrok from "ngrok";

enum TunnelerState {
  CLOSED,
  STARTING,
  OPEN,
  DISCONNECTED,
  ERROR,
}

export default class Tunneler {
  private readonly port: number;
  private ngrokURL?: string;
  private state: TunnelerState = TunnelerState.CLOSED;

  constructor(port: number) {
    this.port = port;
  }

  get publicURL(): string | undefined {
    return this.ngrokURL;
  }

  async open(): Promise<void> {
    switch (this.state) {
      case TunnelerState.CLOSED:
        this.state = TunnelerState.STARTING;
        break;
      case TunnelerState.STARTING:
        throw new Error(
          "Unable to open tunnel, a tunnel is already being opened!"
        );
      case TunnelerState.OPEN:
      case TunnelerState.DISCONNECTED:
        throw new Error(
          "Unable to open tunnel, an open tunnel already exists!"
        );
      case TunnelerState.ERROR:
        throw new Error("Unable to open tunnel, an unexpected error occurred!");
    }
    const url = await ngrok.connect({
      addr: this.port,
      onStatusChange: this.handleNgrokStatusChange,
    });
    this.ngrokURL = url;
    this.state = TunnelerState.OPEN;
  }

  async close(force: boolean = false): Promise<void> {
    if (force) {
      await ngrok.kill();
      return;
    }
    if (this.state !== TunnelerState.OPEN) {
      throw new Error("Unable to close tunnel, no tunnel is open!");
    }
    await ngrok.disconnect();
    this.ngrokURL = undefined;
  }

  private handleNgrokStatusChange = (status: "closed" | "connected"): void => {
    if (status === "closed") {
      this.state = TunnelerState.DISCONNECTED;
    } else {
      this.state = TunnelerState.OPEN;
    }
  };
}
