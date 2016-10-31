# The sign-in experience

NOTE: This document needs to be rewritten in the context of leveraging
Telegram to be the backend chat service.

If the User has never signed in to the app, or a sign-in has gone
stale, for whatever reason, the app will present a confirmation dialog
showing a list of radio button choices indicating the identity name
(text, typically an email address) and the identity provider (icon,
typically Google, Facebook, Twitter, etc).

For example,

```
Select Sign-in
--------------

  o (Facebook icon) fred@somedomain.com
  x (Google+ icon) fred@somedomain.com
  o (Google+ icon) family@gmail.com
  o (Twitter icon) @foobar

--------------

          Cancel OK
```
The title is fixed and the list of identities is in a scrollable list.

After a successful sign-in, the User is presented with a normal screen
in the last visited game room, which very well might be associated
with a game that has ended or an empty chat panel (See normal use
notes).  If there is no such history, the User will be presented with
a set of introductory choices (links) along the lines of:

```
Welcome to GameChat
--------------------

    + Start a new game

    > Join a game already in progress

    Learn more about GameChat

---------------------
```

## Starting a new game

Tapping on this button brings up a confirmation dialog to allow the
User to invite an opponent to a game via Email.  Something along the
lines of:

```
Invite a player or a guest
--------------------------

    User (Name or Email Address): _________________________

    [x] Allow invites from this User
    [x] Allow User to watch my games
        [ ] Just this game
        [x] All my games

--------------------------

          Cancel OK
```

The preference settings will be presented only once, per invited User.
They may be modified from the preferences screen (see Preferences
design notes).

## Joining a game already in progress

Tapping on this button reveals games for which you have been invited
to watch or to play in.  Selecting a game causes the app to present
the normal screen for that game room.  The list will look something
like:

```
...
    < Join a game already in progress

       The Shovel vs ChessWhiz (active) 1/23/2016 5:30pm EST

       Ethan vs Grandpop (pending)

       Ada vs Phoenix (finished) 12/27/2015 2:50pm EST

       Grandpop vs pajato1 2/22/2016 3:17am EST
...
```

Tapping on an item will select that game.

## Getting help

Help will be provided via HTML and rendered on the device accordingly.
No browser involvement is anticipated.

## Other notes

The sign-in process is avoided as much as possible to facilitate quick
and easy app engagement.  This is possible because the app uses
OAuth2 and stores the User identity, identity provider and game
history on the client.
