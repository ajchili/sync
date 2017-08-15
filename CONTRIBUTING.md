## how to contribute
1. Download/fork repository
2. Make changes or add a new features
3. Test these changes or features
	* To run sync, you must setup maven then run these three commands:
		* `clean:clean`
		* `assembly:assembly`
		* `exec:java`
4. Create a pull request, if the change will benefit sync, the code will be pulled
