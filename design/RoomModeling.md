# Notes on Modeling Rooms

There will be two types of rooms: a game room and a private room.

Each game will be associated with a room using some canonical naming scheme.  This type of room is a Game Room (game room, game-room, GameRoom, etc.)  Navigating to a game room (from the Hamburger menu/Navigation drawer) will force the current game screen to show the associated game.  A game room can be active (game is still going on) or inactive (the game has timed out in sone fashion).  By default, only active games are navigable.

Each User will have a private room that is not associated with a particular game room.  The current game screen will remain in place while a User is in a private room.  While in a private room, the User will be able to select a game to view on the game screen using the overflow menu.

When a User navigates to a room that room becomes the "last room" and will be a default if and when the app is recreated.  The set of rooms a User can navigate to will consist of active game rooms the User has created, discovered or been invited to as well as the set of all private rooms the User has implicitly or explicitly been invited to.

A first time User will have no accessible rooms.  To establish some accesible rooms, the User must initiate a game or attempt to enter a private room.  In both cases the other User will be identified by email address.  If the other User is online, the request can be approved immediately.  If not, then an email request will be sent to the other User's email address along with a link to accept the invitation, sort of the LinkedIn model.  In the event the invited User accepts the invitation, that User's private room is now available to the inviting User as will all game rooms the invited User is involved with.
