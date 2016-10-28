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

import static com.pajato.android.gamechat.event.RegistrationChangeEvent.REGISTERED;
import static com.pajato.android.gamechat.event.RegistrationChangeEvent.UNREGISTERED;

/**
 * Provide a thin veneer over the GreenRobot EventBus facility.
 *
 * @author Paul Michael Reilly
 */
public enum AppEventManager {
    instance;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = AppEventManager.class.getSimpleName();

    // Public instance variables.

    /** The EventBus event listener map. */
    private Map<String, Object> mHandlerMap = new HashMap<>();

    // Public instance methods.

    /** Cancel further processing on a given event. */
    public void cancel(final Object event) {
        // All subsequent subscribers will not see the given event.
        EventBus.getDefault().cancelEventDelivery(event);
    }

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
            // Register the new listener both with the handler map and with Firebase.  Also post the
            // registration to anyone who cares.
            mHandlerMap.put(name, handler);
            EventBus.getDefault().register(handler);
            post(new RegistrationChangeEvent(name, REGISTERED));
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
            post(new RegistrationChangeEvent(name, UNREGISTERED));
            Log.d(TAG, String.format(Locale.US, "Unregistered app event listener {%s}.", name));
        }
    }
    /** Unregister all named listeners. */
    public void unregisterAll() {
        // Remove all registered listeners and clear the map.
        for (String name : mHandlerMap.keySet()) {
            Object handler = mHandlerMap.get(name);
            post(new RegistrationChangeEvent(name, UNREGISTERED));
            EventBus.getDefault().unregister(handler);
        }
        mHandlerMap.clear();
        Log.d(TAG, "Unregistered all app event listeners.");
    }

}
