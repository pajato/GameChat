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

package com.pajato.android.gamechat.database.handler;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.game.event.ExpProfileChangeEvent;
import com.pajato.android.gamechat.game.model.ExpProfile;

import java.util.Locale;

import static com.pajato.android.gamechat.database.DatabaseListManager.ChatListType.room;
import static com.pajato.android.gamechat.event.MessageChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.MOVED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.NEW;
import static com.pajato.android.gamechat.event.MessageChangeEvent.REMOVED;

/**
 * Provide a class to handle new and changed experiences inside a group and room.
 *
 * @author Paul Michael Reilly
 */
public class ExpProfileChangeHandler extends DatabaseEventHandler implements ChildEventListener {

    /** The logcat format string. */
    private static final String LOG_FORMAT = "%s: {%s, %s}.";

    /** The logcat TAG. */
    private static final String TAG = ExpProfileChangeHandler.class.getSimpleName();

    // Public constructors.

    /** Build a handler with the given name and path. */
    public ExpProfileChangeHandler(final String name, final String groupKey, final String roomKey) {
        super(name, DatabaseManager.instance.getExpProfilePath(groupKey, roomKey));
    }

    /** Deal with a new message. */
    @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        // Log the event and determine if the data snapshot exists, aborting if it does not.
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildAdded", dataSnapshot, s));
        process(dataSnapshot, NEW);
    }

    /** Deal with a change in an existing message. */
    @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildChanged", dataSnapshot, s));
        process(dataSnapshot, CHANGED);
    }

    @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildRemoved", dataSnapshot, null));
        process(dataSnapshot, REMOVED);
    }

    @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildMoved", dataSnapshot, s));
        if (!dataSnapshot.exists()) return;
        process(dataSnapshot, MOVED);
    }

    /** ... */
    @Override public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }

    // Private instance methods.

    /** Process the change by updating the database list and notifying the app. */
    private void process(final DataSnapshot snapshot, final int type) {
        // Abort if the data snapshot does not exist.
        if (!snapshot.exists()) return;

        // A snapshot exists.  Extract the experience profile from it and handle it based on the
        // change type. Finally notify the app of the change.
        ExpProfile expProfile = snapshot.getValue(ExpProfile.class);
        switch (type) {
            case NEW:
            case CHANGED:
                // Update the database list experience profile map by adding or replacing the entry.
                DatabaseListManager.instance.expProfileMap.put(expProfile.key, expProfile);
                break;
            case REMOVED:
                // Update the database list experience profile map by removing the entry (key).
                DatabaseListManager.instance.expProfileMap.put(expProfile.key, expProfile);
                break;
            case MOVED:
            default:
                // Not sure what a moved change means or what to do about it, so do nothing.
                break;
        }
        AppEventManager.instance.post(new ExpProfileChangeEvent(expProfile, type));
    }
}
