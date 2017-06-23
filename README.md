# sync
a java based mutli-client video and audio syncing application

## limitations
* Only supports .mp4
* No client drop detection
* Lots of bugs
* Still in development
* **Extremely high bandwidth usage**
	* Sync will destroy your bandwidth connection in its current state. Rather than buffering playback and downloading media at a reasonable rate, sync will just download it all. At once. With all of your bandwidth.
* Poor performance
	* _**Currently, the best way to avoid a massive performance hit on a low-end machine is to use fullscreen mode.** This will run two windows but allows media playback to run on a separate thread than the regular gui which increases performance._

## compatibility issues
While some of these issues become irrelevant or are fixed with updates, a server or client may not have the latest stable version

* Version [0.2.0](https://github.com/ajchili/sync/releases/tag/0.2.0)
	* Messaging feature not compatible with any other version
	
## features
* Real-time video and audio synchronization
* Fullscreen playback
* Real-time chat

## setup
1. Check to see if Java is downloaded
	* The easiest way to do this is to go to the [Java Download Page](https://www.java.com/en/download/) and download the latest version. After downloading the latest version, run the installer. This will either install, update, or notify  you that Java is installed.
2. Download [sync](https://www.github.com/ajchili/sync/releases)
	* The latest stable version can be found in the link above
	* The latest development version can be obtained through the repository itself
3. Run sync
	* Server setup
		* It is recommended to specify the amount of ram that the application can use _(it is recommended to use a minimum of 2 Gigabytes of ram and a multi-core cpu)_
		1. Port forward if you will be using sync past a local network, sync uses port `8000`
			* If need be, add sync to your firewall
		2. Obtain your IP Address
			* ~~This can be done by typing `what's my ip` into google~~
			* As of version [0.2.1](https://github.com/ajchili/sync/releases/tag/0.2.1), the IP Address of the server will be displayed in the title of the server window
		3. Ensure your media is of `.mp4` format and accessible by a web browser
		4. Load your media into sync and share your IP Address
		5. Enjoy!
	* Client setup
		1. To prevent any communication issues, it is recommended to add sync to your firewall
		2. Obtain the IP Address of the server you would like to connect to
		3. Enjoy!
