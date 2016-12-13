/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.database;

import com.pajato.android.gamechat.chat.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide a class to manage the app interactions with the database for lists of chat messages.
 * Briefly, a message is received from the database.  It might be a new message (just posted by a
 * User) or a startup message (one of a batch occurring in response to setting a listener on a room)
 * or a paged message (in response to a search or scrolling event initiated by the User). In any
 * case the message is added (as part of a background task) to a sorted list (double buffered) and
 * the background processing of the message begins:
 *
 * 1) if a background process is already running on behalf of another message, this new message is
 * queued and will be batch processed along with any others as soon as the current background
 * process is complete.
 *
 * 2) if there is no background process running, a new one is started.  First, the message is added
 * to the sorted list. Then a data structure allowing for the easy association of date headers (now,
 * recent, today, yesterday, etc.) is computed.  This data structure essentially associates a date
 * header with an index into the list of messages and the number of messages in that date header
 * category.  After this background task is completed the data structure is copied (double
 * buffering) and switched onto the main thread as part of a message change event posting.  That
 * posted event will be processed to display lists of messages per the date header type.
 *
 * @author Paul Michael Reilly
 */
public enum MessageListManager {
    instance;

    // Private class constants.

    /** The message list change handler base name. */
    private static final String MESSAGE_LIST_CHANGE_HANDLER = "messageListChangeHandler";

    // Public instance variables.

    /** A presentation ready collection of messages. */

    /** A sorted (by creation date) list of messages. */
    private List<Message> mMessageList = new ArrayList<>();

    /** A message queue awaiting processing. */
    private List<Message> mMessageQueue = new ArrayList<>();

    // Private instance variables.

    // Public instance methods.
}
