/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
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

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/* Provide the Firebase instance id service. */
public class GameChatInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "GCInstanceIDService";
    private static final String FRIENDLY_ENGAGE_TOPIC = "friendly_engage";

    /** Generate a new instance ID token as the original is now invalid. */
    @Override public void onTokenRefresh() {
        // Log the creation of the new instance id token and subscribe to the topic.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, String.format("GameChat chat token: {%s}.", token));
        FirebaseMessaging.getInstance().subscribeToTopic(FRIENDLY_ENGAGE_TOPIC);
    }

}
