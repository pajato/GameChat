/*
 * Copyright (C) 2017 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.main;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Locale;

/**
 * Provide a service to monitor the message and experience changes in rooms on behalf of registered
 * Users. Changes to rooms in which a current User is "in" (see below) are ignored.  All other
 * changes (for each User registered wih the service) generate or modify a status bar notification.
 * GameChat app settings will allow configuration of notification filtering for each User.  There
 * will only be a single app icon providing notification details per the Android notifications spec
 * fount at: https://developer.android.com/guide/topics/ui/notifiers/notifications.html
 *
 * The service will buffer all database accesses on behalf of registered...
 *
 * A User is currently "in" a room iff GameChat is running in the foreground.  This implies that
 * the service has been informed of the current User (via an account change event).
 *
 * @author Paul Michael Reilly on 2/25/17
 */
public class MainService extends Service {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = "MainService";

    // Public instance methods.

    /** Implement the Service interface by providing a null operation (no binding is done). */
    @Override public IBinder onBind(final Intent intent) {
        logEvent("bind", "not used");
        return null;
    }

    /** Implement the Service interface by initializing the service on startup. */
    @Override public void onCreate() {
        // TODO: figure this out
        logEvent("create", "Creating the main service");
    }

    /** Implement the Service interface by handing the death of the service. */
    @Override public void onDestroy() {
        logEvent("create", "Creating the main service");
        // TODO: figure this out
    }

    /** Implement the Service interface to handle a command. */
    @Override public int onStartCommand(final Intent intent, final int flags, final int startId) {
        String name = intent.getStringExtra("nameKey");
        String message = name != null
            ? String.format(Locale.US, "executing command {%s}.", name)
            : "executing un-named command.";
        logEvent("startCommand", message);
        return START_REDELIVER_INTENT;
    }

    // Private instance methods.

    /** Log an event. */
    private void logEvent(final String name, final String message) {
        Log.d(TAG, String.format(Locale.US, "event {%s}, with message: {%s}.", name, message));
    }
}
