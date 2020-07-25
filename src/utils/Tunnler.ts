import ngrok from 'ngrok';

export class Tunneler {
  private disconnecting: boolean = false;
  private connecting: boolean = false;
  private static instance: Tunneler;
  public url: string | null = null;
  private constructor() {}
  public static get INSTANCE(): Tunneler {
    if (this.instance === undefined) {
      this.instance = new Tunneler();
    }
    return this.instance;
  }
  public get canClose(): boolean {
    return this.url !== null && !this.disconnecting;
  }
  public get canOpen(): boolean {
    return this.url === null && !this.connecting;
  }
  public async close(): Promise<void> {
    if (!this.canClose) {
      throw new Error(
        'Unable to close tunnel, the tunnel is currently connecting/disconnecting!'
      );
    }
    this.disconnecting = true;
    try {
      await ngrok.disconnect();
      this.url = null;
    } catch (err) {
      throw err;
    } finally {
      this.disconnecting = false;
    }
  }
  public async open(options: ngrok.INgrokOptions = {}): Promise<string> {
    if (!this.canOpen) {
      throw new Error(
        'Unable to open tunnel, the tunnel is currently connecting/disconnecting!'
      );
    } else if (this.url !== null) {
      return this.url;
    }
    this.connecting = true;
    try {
      const url = await ngrok.connect(options);
      this.url = url;
      return url;
    } catch (err) {
      throw err;
    } finally {
      this.connecting = false;
    }
  }
}
