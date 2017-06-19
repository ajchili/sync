# sync
a java based mutli-client video and audio syncing application

## limitations
* Only supports .mp4
* No client drop detection
* Many unfound bugs
* Still in development
* Potentially high bandwidth usage
	* _I am currently unable to test the bandwidth usagse of sync, however, the application communicates between clients/server multiple times per second for multiple queries._

## setup
1. Check to see if Java is downloaded
	* The easiest way to do this is to go to the [Java Download Page](https://www.java.com/en/download/) and download the latest version. After downloading the lastest version, run the installer. This will either install, update, or notify  you that Java is installed.
2. Download [sync](https://www.github.com/ajchili/sync)
	* **No released version, for pre-release testing, please fork or download the repository and run though an IDE.**
3. Run sync
	* Server setup
		* It is recommended to specify the amount of ram that the application can use _(it is recommended to use a minimum of 2 Gigabytes of ram)_
		
		1. Port forward if you will be using sync past a local network, sync uses port `8000`
			* If need be, add sync to your firewall
		2. Obtain your IP Address
			* This can be done by typing `what's my ip` into google
		3. Ensure your media is of `.mp4` format and accessable by a web browser
		4. Load your media into sync and share your IP Address
		5. Enjoy
	* Client setup
		* For client setup, specifying the amount of ram is not required
		
		1. To prevent any communication issues, it is recommended to port forward and add sync to your firewall
		2. Obtain the IP Address of the server you would like to connect to
		3. Enjoy!

## how to contriubute
* To contribue to sync, fork and create a pull request. Then, make the changes that you think would benefit the program. If the changes help in any way I will merge your request.