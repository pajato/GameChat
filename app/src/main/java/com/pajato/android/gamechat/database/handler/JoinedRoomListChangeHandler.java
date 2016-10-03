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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.JoinedRoomListChangeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide a class to handle structural changes to a User's set of joined rooms.
 *
 * @author Paul Michael Reilly
 */
public class JoinedRoomListChangeHandler extends DatabaseEventHandler
        implements ValueEventListener {

    // Private instance constants.

    /** The logcat TAG. */
    private final String TAG = JoinedRoomListChangeHandler.class.getSimpleName();

    // Public constructors.

    /** Build a handler with the given name and path. */
    public JoinedRoomListChangeHandler(final String name, final String path) {
        super(name, path);
    }

    /** Get the current set of active rooms using a list of room identifiers. */
    @Override public void onDataChange(final DataSnapshot dataSnapshot) {
        // Determine if any active rooms exist.
        List<String> list = new ArrayList<>();
        GenericTypeIndicator<List<String>> t;
        if (dataSnapshot.exists()) {
            t = new GenericTypeIndicator<List<String>>() {};
            list.addAll(dataSnapshot.getValue(t));
        }
        AppEventManager.instance.post(new JoinedRoomListChangeEvent(list));
    }

    /** ... */
    @Override public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }
}
