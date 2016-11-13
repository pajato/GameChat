Short Term Issues needing resolution.  Longer term items should be logged on GitHub.

- Fix the "Join Developer Groups" bug.
- Implement AddGroupActivityTest#testHomeOverflowMenuItem avoiding the use of the AOSP Navigate Up string resource.
- Create AddGroupActivityTest#testButtonClickHandlerWithAccount() to exercices process().
- Rename the "game" package to "exp".
- Change the FAM for signed out and offline screens to allow for sign in, switch accounts and connecting to the network; fix tests accordingly.
- Morph the options overflow menu items to behave like the Inbox app; Add privacy policy while doing this.
- Set up real web site for GameChat using Google Sites.
- Add member rooms to the room list screens.
- Make <experience> back press navigate to the optimal screen on the experience page.
- Add long press context menus to player controls for experiences to provide alternative choices (computer, another user, a friend, change name, etc.)
- Add a dynamic "goto" FAM option to quickly navigate to the optimal screen on the experience page.
- Use SVG or Unicode chess and checkers pieces.
- Address an AS warning for CheckersFragment#onNewGame().
- Fix Chat sign out crock:  Signing out works OK but signing back in does not display the correct result in either the chat or experience pages.
- Overhaul "Winner" notifications/animations starting with TicTacToe.
- Add an icon (preferable SVG) to the sign in button on the signed out screen.
- Implement invites that allow a User to join a group.
- Implement join a room.
- Implement add a room.
- Setup daily jenkins releases, i.e. release an update every day at a prescribed time when a new feature or fix is in master.
- Add connected checks for tablets.

- Do Play Store V1/Beta release.

- Optimize message fetching to start from the last message read in a room.
- Consolidate ExpDispatcher and ChatDispatcher into a single class using generics.
- Consolidate the offline, no account/signed out, and no experiences/messages layout files into a single layout file.
- Consoldiate ExpFragmentType and ChatFragmentType.
- Rename the "game" package to "exp".

- Do Production release.
