# GameChat
An Android app allowing gamers and gawkers to chat while playing and observing one or more games.

Why use this app?  To have fun while learning or teaching simple games using social chat.

# Building and Running

There are three product types within GameChat: debug, release and stage.  Each can be run in a separate Firebase project.  Use the debug product type with a private Firebase project configured with the SHA-1 fingerprint associated with the debug product type.  Use the stage product type to collaborate with other developers in order to share a database that at one point was a snapshot of the production database.  See the project lead to obtain access to the stage product type google-services.json file and the signing keystore properties file.  This file should be put into the app/src/stage/ folder, either directly or via a link to the Google Team Drive resource.  Do not use the release product type under any circumstances.  This is the province of the project lead/release manager only.
