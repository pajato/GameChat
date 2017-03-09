# Message and Game Change Notifications Design

When a new message is created or a move has been made in a game, Users running the app will see these changes immediately (within milliseconds) assuming they are "in" the room where the changes occur.  If the assumption is wrong, the Users need to be notified using Android's standard status bar notification practice.  The mechanism used to make this work is non-trivial.  This document describes two approaches to implementing this mechanism and the form status bar notifications will take on a given device.

## Notification Server Approach

This approach is based on Firebase Cloud Messaging (FCM, previously known as Google Cloud Messaging, or GCM).

### Overview

When the app is started, it registers the device on which it is running with a cloud based GameChat notification server. The app provides both a device id and sufficient User credentials for the server to authenticate with Firebase. Firebase then uses these credentials to monitor the message and game move state of the rooms the User has joined.  

When state changes are detected, the GameChat server will determine if the User needs to be notified.  If so, the server will provide a Firebase Cloud Messaging push notification to the device.  The notification will be added to the status bar by system software even if the app is not running.

If the app is running when a relevant state change occurs outside of the User's current room(s), the app will generate an Android Snackbar message in addition to the status bar notification.  An app setting will enable or disable this behavior.

The server software will track the last new message and game move for each User.  Periodically, it will connect to the Firebase database to determine if changes have been made and will contact the device registered by the User when and only when necessary.

### Ramifications

For a small market penetration, such a server would be inexpensive to run.  A few thousand Users could easily be handled by a common cloud computing configuration: 4-8GB RAM, 128GB SSD, Quad core processor, ~2.5GHz.  The software itself is fairly simple: an authentication module, a scheduling module to poll the database changes for each User, a registration module and a notification module.

On the other hand. a wildly successful app with millions of Users would likely require a much more complex configuration and a hefty montly bill in order to provide timely updates to the User devices.

### Conclusion

While it is tempting to take this route since it is relatively easy to get off the dime, in the long run, this approach would likely be a severe headache waiting to happen as well as a monitoring and management drain.  Especially in the face of even a moderately successful market penetration.

## Device Service Approach

This approach uses a (persistent) service running in the app to handle notiications.  The service is used to detect new messages and game moves which the User will not see in the current app configuration, i.e. the User is not viewing the room where the new message was created or the game in which the move was made.

### Overview

In this scheme, a service is configured in the app to monitor the need to provide status bar notifications.  While the application's main activity is running in the foreground, the service will listen for new messages and game move changes and place notifcations in the status bar when the User is not "in" the room where the state change occurred.

When the main activity is running in the background, all such changes will result in status bar notifications.

When the main activity is not running, the service switches to a scheduled activity where it will run very briefly at a User selected frequency, ranging from every second to once an hour, something like:

* Once per second
* Once every 15 seconds
* Once every 30 seconds
* Once every minute
* Once every 5 minutes
* Once every 30 minutes
* Once an hour

The service will also, under User control, honor "night-time" and device inactivity so that it will not run at night or when the device is totally inactive.

All of this control is to ensure that the service does not become a battery drain to the User and to empower the User to decide the trade-off between battery drain and timely updates, or between annoyance/distraction and timely updates.

The scheduled activity itself will use persisted credentials to establish a Firebase connection, create listeners to the relevant rooms, process and save change information, update the status bar and shut down.  These scheduled activities should be active for tens of milliseconds or less, for each device User that is using the app.

### Ramifications

An inattentive User could configure a device to cause a serious battery drain problem, or to flood the notification drawer whereas an attentive User could configure the device to do exactly what is desired, providing an optimal experience.  An oblivious User (one who uses defaults, just because) should experience something in between, low battery drain and timely notifications.

The service approach is necessarily more complex than the server approach but not overly so.  Much of the application's code can be shared.  And much of the service code is standard practice with Android.  The Firebase JobDispatcher library provides a compatible facility to support job scheduling.  In recent devices the JobDisatcher uses Android's JobSchedular class to efficiently manage the battery use.

### Conclusion

Eliminating the need for a notification server, creating one less moving part (and a big moving part at that) and the ability to reuse and localize code with the app itself is compelling, even with the risk of battery abuse and more complex code (especially User configuration and job scheduling).  Thus, the service approach carries the day, in our humble opinion.

## Status Bar Notifications Format

Android provides flexibility in the way notifications will be presented.  For GameChat, on each device, there will be a single status bar icon representing changes using the Material Design recommendations.  The notification will have two states: an unexpanded icon in the status bar and  a short summary in the notification drawer which the User can click on to be brought to the relevant page in the app, starting the app as necessary.
