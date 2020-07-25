import { ChildProcess, exec } from 'child_process';

export type InstillationStatus = 'installed' | 'unknown' | 'unverified';

export class ffmpeg {
  private static instance: ffmpeg;
  public installationStatus: InstillationStatus = 'unverified';
  private constructor() {
    this.fetchInstillationStatus();
  }
  public static get INSTANCE(): ffmpeg {
    if (this.instance === undefined) {
      this.instance = new ffmpeg();
    }
    return this.instance;
  }
  public fetchInstillationStatus(): Promise<InstillationStatus> {
    const process: ChildProcess = exec('ffmpeg -version');
    return new Promise((resolve) => {
      process.on('exit', (code: number, _: NodeJS.Signals) => {
        if (code === 127) {
          this.installationStatus = 'unknown';
        } else {
          this.installationStatus = 'installed';
        }
        resolve(this.installationStatus);
      });
    });
  }
}
