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

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.event.MessageChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.MOVED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.NEW;
import static com.pajato.android.gamechat.event.MessageChangeEvent.REMOVED;

/**
 * Provide a class to handle new and changed experiences inside a room.
 *
 * @author Paul Michael Reilly
 */
public class ExperiencesChangeHandler extends DatabaseEventHandler implements ChildEventListener {

    // Public constants.

    /** The logcat format string. */
    private static final String LOG_FORMAT = "%s: {%s, %s}.";

    /** The logcat TAG. */
    private static final String TAG = ExperiencesChangeHandler.class.getSimpleName();

    // Public constructors.

    /** Build a handler with the given name and path. */
    public ExperiencesChangeHandler(final String name, final String path) {
        super(name, path);
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

    /** Return an experience from the given snapshot. */
    private Experience getExperience(@NonNull DataSnapshot snapshot) {
        // Ensure that the snapshot contains a type value.  Abort if not, otherwise return the
        // converted snapshot.
        DataSnapshot typeSnapshot = snapshot.child("type");
        String value = typeSnapshot.getValue(String.class);
        ExpType expType = value != null ? ExpType.valueOf(value) : null;
        if (expType == null)
            return null;

        // Obtain the fragment type from the experience type in order to convert to the right model
        // class.
        @SuppressWarnings("unchecked")
        Experience result = (Experience) snapshot.getValue(expType.experienceClass);
        return result;
    }

    /** Return a map of experiences for the room and group in the given experience. */
    private Map<String, Experience> getExpMap(final Experience experience, int type) {
        // Ensure that the room map exists for the group in the master map, creating it if need be.
        Map<String, Experience> expMap = new HashMap<>();
        Map<String, Map<String, Experience>> roomMap;
        roomMap = ExperienceManager.instance.expGroupMap.get(experience.getGroupKey());
        if (roomMap == null) {
            if (type == REMOVED)
                return expMap;
            // This would be the first room map entry for the group.  Create an empty room map and
            // associate it with the group key.
            roomMap = new HashMap<>();
            ExperienceManager.instance.expGroupMap.put(experience.getGroupKey(), roomMap);
        }

        // The room map now exists if it did not before.  Determine if the experience map needs to
        // be created.
        expMap = roomMap.get(experience.getRoomKey());
        if (expMap == null) {
            expMap = new HashMap<>();
            if (type == REMOVED)
                return expMap;
            // The profile map needs to be created.  Do it now and associate it with the room.
            roomMap.put(experience.getRoomKey(), expMap);
        }
        return expMap;
    }

    /** Process the change by updating the database list and notifying the app. */
    private void process(final DataSnapshot snapshot, final int type) {
        // Validate the snapshot and the experience.  Abort if either is invalid.
        Log.d(TAG, "Processing a data snapshot.");
        Experience experience = snapshot.exists() ? getExperience(snapshot) : null;
        Map<String, Experience> expMap = experience != null ? getExpMap(experience, type) : null;
        if (expMap == null) return;

        // A snapshot exists with a valid experience. Case on the change type to update the cached
        // model.
        String key = experience.getExperienceKey();
        switch (type) {
            case NEW:
            case CHANGED:
                // Add the experience to the list manager (or replace it if it already exists.) in
                // two places: 1) a flat map associating an experience key with an experience
                // object, and 2) a group map associating a group, room, and experience key with
                // the experience object.
                expMap.put(key, experience);
                ExperienceManager.instance.experienceMap.put(key, experience);
                break;
            case REMOVED:
                // Update the database list experience profile map by removing the entry (key).
                expMap.remove(key);
                ExperienceManager.instance.experienceMap.remove(key);
                break;
            case MOVED:
            default:
                // Not sure what a moved change means or what to do about it, so do nothing.
                break;
        }
        AppEventManager.instance.post(new ExperienceChangeEvent(experience, type));
    }
}
