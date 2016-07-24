http://stackoverflow.com/questions/3000214/java-http-client-request-with-defined-timeout
http://stackoverflow.com/questions/10960409/how-do-i-save-a-file-downloaded-with-httpclient-into-a-specific-folder

http://stackoverflow.com/questions/27293960/swipe-to-dismiss-for-recyclerview

http://stackoverflow.com/questions/8989099/how-to-communicate-a-service-with-a-running-thread-in-android

http://stackoverflow.com/questions/3841317/how-to-see-if-wifi-is-connected-in-android

http://stackoverflow.com/a/7908446
http://stackoverflow.com/a/23686083

Exclude partial fields GSON:

http://www.javacreed.com/gson-annotations-example/
https://google.github.io/gson/apidocs/com/google/gson/GsonBuilder.html


* According to this link, there is no timeout by default:

    https://hc.apache.org/httpcomponents-client-ga/httpclient/xref/org/apache/http/client/config/RequestConfig.html

TODO lib:

* Add request profiling to WebServer using format "GET /api/polling/ok 200 5019.171 ms - 17"
* Sync state of peer on event
* Prevent server from starting if network is not available
* Remove sendJSON() and add encode() to *Repository
* Wrap all listener call in try-catch block
* Give method to check server status and error message on service
* Fix last accessed time (we need to serialize it when saving peers but not sending peers)
* NFC/Beaming

TODO self:

* Add app icon
* Prevent click on peer when offline
* Remove peer from list
* List files from peer
* Download file from peer
* Notification when downloading/finished
* Remove open directory button from menu and use a fab instead
* For PeerFilesFragment, use encode()/decode() to get complete PeerEntity object instead of just the host

DONE:

* Event to update display name
* Event to update location

WONT FIX:

* Class to handle GET file list and file download
