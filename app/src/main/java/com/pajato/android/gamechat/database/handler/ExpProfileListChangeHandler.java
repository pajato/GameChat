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
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ExpProfileListChangeEvent;
import com.pajato.android.gamechat.game.model.ExpProfile;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.event.MessageChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.MOVED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.NEW;
import static com.pajato.android.gamechat.event.MessageChangeEvent.REMOVED;

/**
 * Provide a class to handle new and changed experience profiles inside a room.
 *
 * @author Paul Michael Reilly
 */
public class ExpProfileListChangeHandler extends DatabaseEventHandler
        implements ChildEventListener {

    // Public constants.

    /** The logcat format string. */
    private static final String LOG_FORMAT = "%s: {%s, %s}.";

    /** The logcat TAG. */
    private static final String TAG = ExpProfileListChangeHandler.class.getSimpleName();

    // Public constructors.

    /** Build a handler with the given name and path. */
    public ExpProfileListChangeHandler(final String name, final String groupKey,
                                       final String roomKey) {
        super(name, DatabaseManager.instance.getExpProfilesPath(groupKey, roomKey));
    }

    // Public instance methods.

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
        process(dataSnapshot, MOVED);
    }

    /** TODO: get a grip on these... */
    @Override public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }

    // Private instance methods.

    /** Return a map of experience profiles for the room in the given experience profile. */
    private Map<String, ExpProfile> getExpProfileMap(final ExpProfile expProfile) {
        // Ensure that the room map exists for the group in the master map, creating it if need be.
        Map<String, ExpProfile> expProfileMap;
        Map<String, Map<String, ExpProfile>> roomMap;
        if (!DatabaseListManager.instance.expProfileMap.containsKey(expProfile.groupKey)) {
            // This would be the first entry for the group.  Create an empty room map and associate
            // it with the group key.
            roomMap = new HashMap<>();
            DatabaseListManager.instance.expProfileMap.put(expProfile.groupKey, roomMap);
        } else {
            // This would be an additional profile for the group.  Determine if it is the first for
            // the room.
            roomMap = DatabaseListManager.instance.expProfileMap.get(expProfile.groupKey);
            if (roomMap == null) {
                // It is the first profile for the room.  Create a map for the room and associate it
                // with the group.
                roomMap = new HashMap<>();
                DatabaseListManager.instance.expProfileMap.put(expProfile.groupKey, roomMap);
            }
        }

        // The room map now exists if it did not before.  Determine if the profile map needs to be
        // created.
        expProfileMap = roomMap.get(expProfile.roomKey);
        if (expProfileMap == null) {
            // The profile map needs to be created.  Do it now and associate it with the room.
            expProfileMap = new HashMap<>();
            roomMap.put(expProfile.roomKey, expProfileMap);
        }

        return expProfileMap;
    }

    /** Process the change by updating the database list and notifying the app. */
    private void process(final DataSnapshot snapshot, final int type) {
        // Abort if the data snapshot does not exist.
        if (!snapshot.exists()) return;

        // A snapshot exists.  Extract the experience profile from it and determine if the profile
        // has been handled already.  If not, then case on the change type and notify the app.
        ExpProfile expProfile = snapshot.getValue(ExpProfile.class);
        Map<String, ExpProfile> expProfileMap = getExpProfileMap(expProfile);
        switch (type) {
            case NEW:
            case CHANGED:
                // Add the profile to the list manager (or replace it if it already exists.)
                expProfileMap.put(expProfile.key, expProfile);
                break;
            case REMOVED:
                // Update the database list experience profile map by removing the entry (key).
                expProfileMap.remove(expProfile.key);
                break;
            case MOVED:
            default:
                // Not sure what a moved change means or what to do about it, so do nothing.
                break;
        }
        AppEventManager.instance.post(new ExpProfileListChangeEvent(expProfile, type));
    }
}
