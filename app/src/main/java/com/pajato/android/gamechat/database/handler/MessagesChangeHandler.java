/*
 * Copyright (C) 2016 Pajato Technologies LLC.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see http://www.gnu.org/licenses
 */

package com.pajato.android.gamechat.database.handler;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.MessageChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.event.MessageChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.MOVED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.NEW;
import static com.pajato.android.gamechat.event.MessageChangeEvent.REMOVED;

/**
 * Provide a class to handle changes to the messages node.
 *
 * @author Paul Michael Reilly
 */
public class MessagesChangeHandler extends DatabaseEventHandler implements ChildEventListener {

    // Private instance constants.

    /** The logcat format string. */
    private static final String LOG_FORMAT = "%s: {%s, %s}.";

    /** The logcat tag. */
    private static final String TAG = MessagesChangeHandler.class.getSimpleName();

    // Private instance variables.

    /** The group and room data. */
    private String mGroupKey;

    /** The room key. */
    private String mRoomKey;

    /** A list of received messages used to filter out activity lifecycle event dupes. */
    private List<String> mMessageList = new ArrayList<>();

    // Public constructors.

    /** Build a handler with the given name and path. */
    public MessagesChangeHandler(final String name, final String groupKey, final String roomKey) {
        super(name, DatabaseManager.instance.getMessagesPath(groupKey, roomKey));
        mGroupKey = groupKey;
        mRoomKey = roomKey;
    }

    /** Deal with a new message. */
    @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        // Log the event and determine if the data snapshot exists, aborting if it does not.
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildAdded", dataSnapshot, s));
        process(dataSnapshot, true, NEW);
    }

    /** Deal with a change in an existing message. */
    @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildChanged", dataSnapshot, s));
        process(dataSnapshot, false, CHANGED);
    }

    @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildRemoved", dataSnapshot, null));
        process(dataSnapshot, false, REMOVED);
    }

    @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildMoved", dataSnapshot, s));
        if (!dataSnapshot.exists()) return;
        process(dataSnapshot, false, MOVED);
    }

    /** ... */
    @Override public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }

    // Private instance methods.

    /** Determine if the given message is a duplicate. */
    private boolean isDupe(final Message message) {
        // Determine if the message has already been received.  The message name is the push key
        // value, hence unique.
        String key = message.key;
        if (mMessageList.contains(key))
            // It has been received.  Flag it.
            return true;

        // The message has not been received.  Add it to the list for subsequent filtering.
        mMessageList.add(key);
        return false;
    }

    /** Process the change by determining if an app event should be posted. */
    private void process(final DataSnapshot snapshot, final boolean filter, final int type) {
        // Abort if the data snapshot does not exist.
        if (!snapshot.exists()) return;

        // Build the message and process the filter flag by checking for a duplicated message.
        // Duplicate messages have been detected by GameChat and is mentioned on StackOverflow:
        // http://stackoverflow.com/questions/38206413/firebase-childeventlistener-onchildadded-adding-duplicate-objects
        Message message = snapshot.getValue(Message.class);
        if (filter && isDupe(message)) return;

        // The event should be propagated to the app.
        AppEventManager.instance.post(new MessageChangeEvent(mGroupKey, mRoomKey, message, type));
    }

}
