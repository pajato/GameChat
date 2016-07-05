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

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class GameChatMessagingService extends FirebaseMessagingService {

    private static final String TAG = "GCMessagingService";

    @Override public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of GameChat messages.
        Log.d(TAG, "GameChat Message Id: " + remoteMessage.getMessageId());
        Log.d(TAG, "GameChat Notification Message: " + remoteMessage.getNotification());
        Log.d(TAG, "GameChat Data Message: " + remoteMessage.getData());
    }

}
