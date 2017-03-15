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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ProfileRoomChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.services.events.EventsManager;

/* Provide the Firebase instance id service. */
public class GameChatInstanceIdService extends FirebaseInstanceIdService {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = GameChatInstanceIdService.class.getSimpleName();

    /** The device token shared preference key. */
    private static final String DEVICE_TOKEN_KEY = "deviceTokenKey";

    // Private instance variables.

    /** The list of joined rooms. */
    private Map<String, Room> mJoinedRooms = new HashMap<>();

    /** The previous token, if any. */
    private String mSavedToken;

    /** The new (current) token. */
    private String mToken;

    // Public constructor.

    /** Build an instance. */
    @Override public void onCreate() {
        mToken = getToken();
        AppEventManager.instance.register(this);
    }

    // Public instance methods.

    /** Handle a room profile change by updating the map. */
    @Subscribe public void onRoomProfileChange(@NonNull final ProfileRoomChangeEvent event) {
        // TODO: handle a deleted room?  I think not.
        // Ensure that the given room is joined.  Abort if not.
        Room room = event.room;
        String id = AccountManager.instance.getCurrentAccountId();
        if (id == null || !room.getMemberIdList().contains(id))
            return;

        // Register the joined room and ensure that it is using the token for the current device.
        mJoinedRooms.put(event.key, room);
        List<String> deviceTokenList = room.memberTokenMap.get(id);
        if (deviceTokenList == null)
            deviceTokenList = getDeviceTokenList(room, id);
        if (deviceTokenList.contains(mToken))
            return;

        // Determine if the new token should be added or replace a previous one.
        if (deviceTokenList.contains(mSavedToken))
            replaceToken(room);
        else
            addToken(room);
    }

    /** Return an empty list that has been associated with the given id in the given room. */
    private List<String> getDeviceTokenList(@NonNull final Room room, @NonNull final String id) {
        List<String> result = new ArrayList<>();
        room.memberTokenMap.put(id, result);
        return result;
    }

    /** Generate a new instance ID token as the original is now invalid. */
    @Override public void onTokenRefresh() {
        // Log the refresh event with the new instance id token.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, String.format("GameChat device token refresh event: {%s}.", token));

        // Handle the case where the new token is the same as the current token by doing nothing.
        if (mToken != null && mToken.equals(token))
            return;

        // Handle the case where the new token becomes current, the previous current becomes the
        // saved token and all the rooms are updated with the change.
        if (mToken != null) {
            mSavedToken = mToken;
            mToken = token;
            putToken(token);
            for (Room room : mJoinedRooms.values())
                replaceToken(room);
            return;
        }

        // Handle the case where the new token is the only token.
        mToken = token;
        putToken(token);
        for (Room room : mJoinedRooms.values())
            addToken(room);
    }

    // Private instance methods.

    /** Replace the saved token with the new token in the given room. */
    private void addToken(final Room room) {
        String id = AccountManager.instance.getCurrentAccountId();
        List<String> tokenList = room.memberTokenMap.get(id);
        tokenList.add(mToken);
        RoomManager.instance.updateRoomProfile(room);
    }

    /** Return null or the current stored device token. */
    private String getToken() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        return prefs.getString(DEVICE_TOKEN_KEY, null);
    }

    /** Return null or the current stored device token. */
    private void putToken(final String value) {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(DEVICE_TOKEN_KEY, value);
        editor.apply();
    }

    /** Replace the saved token with the new token in the given room. */
    private void replaceToken(final Room room) {
        String id = AccountManager.instance.getCurrentAccountId();
        List<String> tokenList = room.memberTokenMap.get(id);
        tokenList.remove(mSavedToken);
        addToken(room);
    }
}
