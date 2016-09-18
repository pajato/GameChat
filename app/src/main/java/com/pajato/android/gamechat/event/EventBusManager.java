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

package com.pajato.android.gamechat.event;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public enum EventBusManager {
    instance;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = EventBusManager.class.getSimpleName();

    // Public instance variables.

    /** The EventBus event listener map. */
    private Map<String, Object> mHandlerMap = new HashMap<>();

    // Public instance methods.

    /** Post an event using the GreenRobot library. */
    public void post(final Object event) {
        EventBus.getDefault().post(event);
        String name = event != null ? event.getClass().getSimpleName() : "null";
        Log.d(TAG, String.format(Locale.US, "Posted event {%s}.", name));
    }

    /** Register a given value event listener. */
    public void register(final Object handler) {
        // Determine if there is already a listener registered with this name.
        String name = handler.getClass().getName();
        if (!mHandlerMap.containsKey(name)) {
            // Register the new listener both with the handler map and with Firebase.
            mHandlerMap.put(name, handler);
            EventBus.getDefault().register(handler);
            Log.d(TAG, String.format(Locale.US, "Registered app event listener {%s}.", name));
        }
    }

    /** Unregister a named listener. */
    public void unregister(final Object handler) {
        // Determine if there is a handler registered by the given name.
        String name = handler.getClass().getName();
        if (mHandlerMap.containsKey(name)) {
            // There is.  Remove it both from the map and as a listener.
            EventBus.getDefault().unregister(handler);
            mHandlerMap.remove(name);
            Log.d(TAG, String.format(Locale.US, "Unregistered app event listener {%s}.", name));
        }
    }

}
