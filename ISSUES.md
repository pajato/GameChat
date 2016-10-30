Short Term Issues needing resolution.  Longer term items should be logged on GitHub.

Fixed for version 4:

- Resolve duplicate messages being shown in the message list view.

Fixed for version 3:

- Resolve Lifecycle bug whereby database handler are not being re-registered (apparently).
- Move Chat fragments to a resume/pause registration model.

Fixed for version 2:

- Remove the "Register" button from the intro screen layout; change the text accordingly.
- Make sure that a click on the button, the icon and layout in the chat FAM generate an appropriate action.
- Make sure that standard material icons use a tint and that non-standard do mot.
- Use the name when reporting a TTT win rather than X or O.
- Make an implicit restart after a win or tie work correctly.

Not fixed:
- Revive the connected tests towards an end of having broken tests fixed and a > 90% coverage level, an ongoing task.
- Add connected checks for tablets.
- Set up real web site for GameChat using Google Sites.
- Add privacy policy to app ala Inbox (options menu Help and Feedback).
- Add member rooms to the room list screens.
- Make <experience> back press navigate to the optimal screen on the experience page.
- Add long press context menus to player controls for experiences to provide alternative choices (computer, another user, a friend, change name, etc.)
- Add a dynamic "goto" FAM option to quickly navigate to the optimal screen on the experience page.
- Use SVG or Unicode chess and checkers pieces.
- Address an AS warning for CheckersFragment#onNewGame().
- Fix Chat sign out crock:  Signing out works OK but signing back in does not display the correct result in either the chat or experience pages.
- Overhaul "Winner" notifications/animations starting with TicTacToe.
- Add an icon (preferable SVG) to the sign in button on the signed out screen.
- Implement join a room.
- Implement add a room.
- Setup daily jenkins releases, i.e. release an update every day at a prescribed time when a new feature or fix is in master.
- Do Play Store Beta release.
- Do Production release.
- Optimize message fetching to start from the last message read in a room.
