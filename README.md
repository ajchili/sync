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
	* The latest experimental version can be obtained through the [experimental](https://github.com/ajchili/sync/tree/experimental) branch
		* _Please note, this branch is most likely broken and will contain unfinished features_
4. Run sync
	* Server setup
		* It is recommended to:
			* Add sync to your firewall _(to ensure that Tomcat will run flawlessly, allow all Java entries to be permitted)_
			* Specify the amount of ram that the application can use _(2 gigabytes of ram minimum)_
			* Use a multi-core cpu
			* Have an unresricted and a beefy internet connection _(the beefy internet connection is only required when using offline media)_
			* Use 720p content to prevent excessive bandwidth usage this can be done by looking at [this](https://github.com/ajchili/sync/issues/8) issue
		1. Port forward if you will be using sync past a local network, sync uses ports `8000` and `8080`
			* Please look at the [Port Forward](https://portforward.com/router.htm) website if you do not know how to port forward or need any help port forwarding
			* _Please note, port forwarding port `8080` is only required for offline media_
		2. Obtain your IP Address, this will be in the title of the sync server window
		3. Load your media into sync and share your IP Address
            1. Click `sync` in the menu of the server
            2. Select media type
                * For online media, click `Set Media URL` and provide the link
                    * _Should your media link be anything other than `http://`, please provide that_
                * For offline media, click `Set Media File` and select your file
		4. Enjoy
	* Client setup
		* It is recommended to:
			* Add sync to your firewall
		1. Obtain the IP Address of the server you would like to connect to
		2. Connect to the server
		3. Enjoy

## compatibility issues
While some of these issues become irrelevant or are fixed with updates, a server or client may not have the latest stable version

* Version [1.3.2](https://github.com/ajchili/sync/releases/tag/1.3.2)
	* Offline media not accessible to clients if URL encoding is required
* Version [1.3.0](https://github.com/ajchili/sync/releases/tag/1.3.0)
	* Incompatible with previous versions of sync as server/client communications have been changed
    * Offline media not accessible to clients
* Version [1.2.0](https://github.com/ajchili/sync/releases/tag/1.2.0)
    * Offline media not accessible to clients
* Version [1.1.2](https://github.com/ajchili/sync/releases/tag/1.1.2)
	* Incompatible with previous versions of sync as server/client communications have been changed
* Version [0.4.0](https://github.com/ajchili/sync/releases/tag/0.4.0) & [0.5.0](https://github.com/ajchili/sync/releases/tag/0.5.0)
	* No OS X support
* Version [0.3.2](https://github.com/ajchili/sync/releases/tag/0.3.2)
	* No longer compatible with future versions as the media player has been changed
* Version [0.2.0](https://github.com/ajchili/sync/releases/tag/0.2.0)
	* Messaging feature incompatible with other version
	
## what to do if you run into issues
If you for any reason run into issues while using sync _(it is possible to as the application is ever changing)_, submit an issue [here](https://github.com/ajchili/sync/issues) and provide as much detail as possible as to the issue
