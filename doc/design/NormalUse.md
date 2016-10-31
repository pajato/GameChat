# Typical usage

A typical scenario is that a User starts the GameChat app.  The User
has previously signed in and selected a game room.  The app will
present a split screen forced to landscape orientation.
(Supporting portrait orientation is a tbd discussion).

Each of the two panels will be of equal and fixed size.  The left
panel will show an ongoing chat modeled in the style of Slack,
HipChat, etc.  The right panel will show an active chess game.

The pair of panels represent a game room.  The Users in the game room
will consist of players and watchers.

## Chat Panel

The Users are shown by nickname (if selected) or real name in the chat
panel as a prefix to every message.  For example,

```
Grandpop:
    Nice move Conor!
ChessWhiz (Black):
    Thanks Grampa.  I don't think Aidan will get out of this one.
The Shovel (White):
    We'll see about that...
Ethan:
    I've got the winner.
Grandpop:
    Ethan, I'll play a game with you while these guys finish up.
    It could take a while.
Ethan: OK, I'll take white.
Grandop leaves.
Ethan leaves.
```

Players are tagged with their color.  Icons (avatars) and
fontification will be used to provide a beautiful design.

## Game Panel

The chess panel will show the active game and will provide an
interactive experience for the players modeled after the Android
ChessFree app.

## Main Menu

A main menu will be provided (Action Bar) to allow the User to switch
identity, join another game room, start a new game, enter a private
chat, set preferences (settings), get help, exit the app, etc.
