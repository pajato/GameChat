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

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.pajato.android.gamechat.database.AccountManager;

/**
 * Provide an intent service to have the Firebase Cloud Messaging servers notify interested room
 * members of a new message or a game move using push messaging.  If a member has the room open in
 * the foreground then that member is not notified.  Nor is the member sending the message.
 *
 * @author Paul Michael Reilly on 3/15/17
 */
public class MainService extends IntentService {

    // Public class constants.

    /** Provide an intent key the the room push value. */
    public static final String ROOM_KEY = "roomKey";

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = MainService.class.getSimpleName();

    // Public constructor

    /** Build an instance to name the worker thread. */
    public MainService() {
        super("NotificationIntentService");
    }

    // Public instance methods.

    /** Handle the intent providing the notification data in the worker thread. */
    @Override protected void onHandleIntent(Intent intent) {
        // Ensure that there is a room key provided by the intent and that it is not the Me room.
        // Abort if there is no key or the the key is the Me room key.
        String roomKey = intent.getStringExtra(ROOM_KEY);
        if (roomKey == null || roomKey.equals(AccountManager.instance.getMeRoomKey()))
            return;
        Log.d(TAG, "Handling the notifications task.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Log.d(TAG, "Handled the notifications task.");
    }
}
