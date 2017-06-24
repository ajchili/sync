# sync
a Java based multi-client video and audio syncing application

## limitations
* No client drop detection
* Currently does not work with OSX
	* This will be fixed with a future update, this may take some time as a custom video player would need to be made. Look [here](https://github.com/caprica/vlcj-player/issues/3) for more information.
* Lots of bugs
* Still in development
* Poor YouTube video playback
	* Both the server and client experience poor video playback when using media from YouTube
* _Potentially_ poor performance
	* Performance after switching from JavaFX to vlcj has yet to be tested

## compatibility issues
While some of these issues become irrelevant or are fixed with updates, a server or client may not have the latest stable version

* Version [0.3.2](https://github.com/ajchili/sync/releases/tag/0.3.2)
	* No longer compatible with future versions as the media player has been changed
	* Last version compatible with OS X, see limitations for more information
* Version [0.2.0](https://github.com/ajchili/sync/releases/tag/0.2.0)
	* Messaging feature not compatible with any other version
	
## setup
1. Check to see if Java is downloaded
	* The easiest way to do this is to go to the [Java Download Page](https://www.java.com/en/download/) and download the latest version. After downloading the latest version, run the installer. This will either install, update, or notify you that Java is installed.
2. Download [vlc](http://www.videolan.org/vlc/index.html)
3. Download [sync](https://www.github.com/ajchili/sync/releases)
	* The latest stable version can be found in the link above
	* The latest development version can be obtained through the repository itself
4. Run sync
	* Server setup
		* It is recommended to specify the amount of ram that the application can use _(it is recommended to use a minimum of 2 Gigabytes of ram, a multi-core cpu, _and a beefy internet connection. **The beefy internet connection is only required when using offline media.**_)_
		1. Port forward if you will be using sync past a local network, sync uses ports `8000` and `8080`
			* If need be, add sync to your firewall
			* **Please note, you DO NOT need to port forward port `8080` if you intend to use online media. However, if you intend on sharing media from your device, this port must be forwarded.**
		2. Obtain your IP Address
			* ~~This can be done by typing `what's my ip` into google~~
			* As of version [0.2.1](https://github.com/ajchili/sync/releases/tag/0.2.1), the IP Address of the server will be displayed in the title of the server window
		3. Obtain your media
			* Online media
				1. Obtain the url of the media you would like to play
				2. Put link _(including `http://` or `https://`)_ into the url field
			* Offline media
				1. Select and copy our media file
				2. Navigate to `/tomcat/webapp/media/`
				3. Paste media in folder
				4. Put link _(formatting it to be `media/FILENAME.EXTENSION`)_ into the url field
		4. Load your media into sync and share your IP Address
		5. Enjoy!
	* Client setup
		1. To prevent any communication issues, it is recommended to add sync to your firewall
		2. Obtain the IP Address of the server you would like to connect to
		3. Enjoy!
