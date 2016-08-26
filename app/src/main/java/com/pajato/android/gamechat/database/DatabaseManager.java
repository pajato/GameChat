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

package com.pajato.android.gamechat.database;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public enum DatabaseManager {
    instance;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = DatabaseManager.class.getSimpleName();

    // Public instance variables.

    /** The Firebase value event listener map. */
    private Map<String, DatabaseEventHandler> handlerMap = new HashMap<>();

    // Public instance methods.

    /** Register a given value event listener. */
    public void registerHandler(final DatabaseEventHandler handler) {
        // Determine if there is already a listener registered with this name.
        String name = handler.name;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(handler.path);
        DatabaseEventHandler registeredHandler = handlerMap.get(name);
        removeEventListener(database, registeredHandler);

        // Register the new listener both with the handler map and with Firebase.
        handlerMap.put(name, handler);
        ValueEventListener valueEventListener = handler instanceof ValueEventListener ?
            (ValueEventListener) handler : null;
        ChildEventListener childEventListener = handler instanceof ChildEventListener ?
            (ChildEventListener) handler : null;
        handlerMap.put(name, handler);
        if (valueEventListener != null) {
            database.addValueEventListener(valueEventListener);
        }
        if (childEventListener != null) {
            database.addChildEventListener(childEventListener);
        }
    }

    /** Unregister all listeners. */
    public void unregisterAll() {
        // Walk the set of registered handlers to remove them, then clear the map.
        DatabaseReference database;
        DatabaseEventHandler handler;
        for (String name : handlerMap.keySet()) {
            handler = handlerMap.get(name);
            database = FirebaseDatabase.getInstance().getReference(handler.path);
            removeEventListener(database, handler);
        }
        handlerMap.clear();
    }

    /** Unregister a named listener. */
    public void unregisterHandler(final String name) {
        // Determine if there is a handler registered by the given name.
        DatabaseEventHandler handler = handlerMap.get(name);
        if (handler != null) {
            // There is.  Remove it both from the map and as a listener.
            DatabaseReference database = FirebaseDatabase.getInstance().getReference(handler.path);
            removeEventListener(database, handler);
            handlerMap.remove(name);
        }
    }

    /** Store an object on the database using a given path, pushKey, and properties. */
    public void updateChildren(final DatabaseReference database, final String path,
                               final String pushKey, final Map<String, Object> properties) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(path + pushKey, properties);
        database.updateChildren(childUpdates);
    }

    /** Store an object on the database using a given path, pushKey, and properties. */
    public void updateChildren(final DatabaseReference database, final String path,
                               final Map<String, Object> properties) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(path, properties);
        database.updateChildren(childUpdates);
    }

    // Private instance methods.

    /** Remove the database event listener, if any is found associated with the given handler. */
    private void removeEventListener(final DatabaseReference database,
                                     final DatabaseEventHandler handler) {
        ValueEventListener valueEventListener;
        ChildEventListener childEventListener;
        if (handler != null) {
            // There is a handler found.  Remove it.
            if (handler instanceof ValueEventListener) {
                valueEventListener = (ValueEventListener) handler;
                database.removeEventListener(valueEventListener);
            } else if (handler instanceof ChildEventListener) {
                childEventListener = (ChildEventListener) handler;
                database.removeEventListener(childEventListener);
            } else {
                // Remove the funky entry, after logging it.
                String format = "Removing an invalid event listener, %s/%s (name/type).";
                String name = handler.name;
                String type = handler.getClass().getSimpleName();
                Log.e(TAG, String.format(Locale.getDefault(), format, name, type));
            }
        }
    }

    public boolean isRegistered(String name) {
        return handlerMap.containsKey(name);
    }
}
