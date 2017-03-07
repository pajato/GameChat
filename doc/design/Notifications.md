# Message and Game Change Notifications Design

When a new message is created or a move has been made in a game, Users running the app will see these changes immediately (within milliseconds to be precise) assuming they are "in" the room where the changes occur.  If the assumption is wrong, the Users need to be notified using Android's standard status bar notification practice.  The mechanism used to make this work is non-trivial.  This document describes this mechanism.

## GameChat Message Server

When the app is started, it registers the device on which it is running with a cloud based GameChat message server providing both a device id and sufficient User credentials for the server to authenticate with Firebase and monitor the message and game move state of the rooms that User has joined.  When state changes are detected, the GameChat server will determine if the User needs a notification.  If so, the server will provide a Firebase Cloud Messaging push notification to the device.  The notification will be added to the status bar by system software even if the app is not running whatsoever.

If the app is running when a relevant state change occurs outside of the User's current room(s), the app will generate an Android Snackbar message in addition to the status bar notification.  An app setting will enable or disable this behavior.

The server softare will track the last new message and game move for each User.  Periodically, it will connect to the Firebase database to determine if changes have been made and will contact the device registered by the User when and only when necessary.
