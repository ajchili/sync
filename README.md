# sync
a Java based multi-client video and audio syncing application
	
## setup
1. Check to see if Java is installed
	1. The easiest way to do this is to go to the [Java Download Page](https://www.java.com/en/download/) and download the latest version
	2. After downloading the latest version, run the installer
	3. This will either install, update, or notify you that Java is installed
2. Download and install [vlc](http://www.videolan.org/vlc/index.html)
3. Download [sync](https://www.github.com/ajchili/sync/releases)
	* The latest stable version can be found in the link above
	* The latest development version can be obtained through the repository itself
4. Run sync
	* Server setup
		* It is recommended to:
			* Add sync to your firewall _(to ensure that Tomcat will run flawlessly, allow all Java entries to be permitted)_
			* Specify the amount of ram that the application can use _(2 gigabytes of ram minimum)_
			* Use a multi-core cpu
			* Have an unresricted and a beefy internet connection _(the beefy internet connection is only required when using offline media)_
		1. Port forward if you will be using sync past a local network, sync uses ports `8000` and `8080`
			* Please look at the [Port Forward](https://portforward.com/router.htm) website if you do not know how to port forward or need any help port forwarding
			* _Please note, port forwarding port `8080` is only required for offline media_
		2. Obtain your IP Address, this will be in the title of the sync server window
		3. Obtain your media
			* Online media
				1. Obtain the url of the media you would like to play
				2. Put link _(including `http://` or `https://`)_ into the url field
			* Offline media
				1. Copy media into the Tomcat folder _(the location of this folder will be shown to you on launch)_
				2. Click `Choose file`
				3. Select the media file you would like to watch
		4. Load your media into sync and share your IP Address
		5. Enjoy!
	* Client setup
		* It is recommended to:
			* Add sync to your firewall
		1. Obtain the IP Address of the server you would like to connect to
		2. Connect to the server
		3. Enjoy!

## compatibility issues
While some of these issues become irrelevant or are fixed with updates, a server or client may not have the latest stable version

* Version [1.1.2](https://github.com/ajchili/sync/releases/tag/1.1.2)
	* Incompatible with previous versions of sync as server/client communications have been changed
* Version [0.4.0](https://github.com/ajchili/sync/releases/tag/0.4.0) & [0.5.0](https://github.com/ajchili/sync/releases/tag/0.5.0)
	* No OS X support
* Version [0.3.2](https://github.com/ajchili/sync/releases/tag/0.3.2)
	* No longer compatible with future versions as the media player has been changed
* Version [0.2.0](https://github.com/ajchili/sync/releases/tag/0.2.0)
	* Messaging feature incompatible with other version
