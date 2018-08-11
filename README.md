# sync
A Java based multi-client video and audio syncing application.
	
## setup
1. Check to see if Java is installed
   1. The easiest way to do this is to go to the [Java Download Page](https://www.java.com/en/download/) and download the latest version
   2. After downloading the latest version, run the installer
   3. This will either install, update, or notify you that Java is installed
2. Download and install [VLC](http://www.videolan.org/vlc/index.html)
3. Download [sync](https://www.github.com/ajchili/sync/releases)
   * The latest stable version can be found in the link above
   * For development builds please download the master branch and view [CONTRIBUTING.md](https://github.com/ajchili/sync/blob/master/CONTRIBUTING.md)
4. Run sync
   * Server setup
      * Please note that UPnP is required for sync to work past a local network
         * You must enable UPnP in your router _(please look at the [Port Forward](https://portforward.com/router.htm) website if you do not know how to enable UPnP)_
      * It is recommended to:
         * Add sync to your firewall _(to ensure that Tomcat will run flawlessly, allow all Java entries to be permitted)_
         * Specify the amount of ram that the application can use _(2 gigabytes of ram minimum)_
         * Use a multi-core cpu
         * Have an unresricted and a beefy internet connection
         * Use 720p content to prevent excessive bandwidth usage and smooth playback
            * Should your media be in a higher resolution, look at [this](https://github.com/ajchili/sync/issues/8) issue for more information
      1. Obtain your IP Address
         * Located either in the title of the window or under `sync` -> `Share Server Address` in the menu bar
      2. Load your media into sync and share your IP Address
         1. Click `sync` -> `Set Media` in the menu of the server
         2. Select media type
            * For online media, click `Set Media URL` and provide the link
               * _Should your media link be anything other than `http://`, please provide that_
            * For offline media, click `Set Media File` and select your file
     3. Enjoy
   * Client setup
      * It is recommended to:
         * Add sync to your firewall
      1. Obtain the IP Address of the server you would like to connect to
      2. Connect to the server
      3. Enjoy
	
## what to do if you run into issues
If you for any reason run into issues while using sync, submit an issue [here](https://github.com/ajchili/sync/issues) and use the bug report template
