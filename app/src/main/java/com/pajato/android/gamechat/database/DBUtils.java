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

package com.pajato.android.gamechat.database;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.InvitationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provide a class to manage the app interactions with the database for lists of members,
 * chat messages, chat rooms, and chat groups.
 *
 * @author Paul Michael Reilly
 */
public enum DBUtils {
    instance;

    // Public class constants.

    // Lookup keys.
    public static final String DEFAULT_ROOM_NAME_KEY = "defaultRoomNameKey";
    public static final String SYSTEM_NAME_KEY = "systemNameKey";
    public static final String WELCOME_MESSAGE_KEY = "welcomeMessageKey";

    // Private instance variables.

    /** The map providing localized resources, setup during initialization. */
    private Map<String, String> mResourceMap = new HashMap<>();

    // Public instance methods.

    /** Get the list data to be displayed by a list adapter for a given list type. */
    public List<ChatListItem> getList(@NonNull final FragmentType type, final ChatListItem item) {
        switch (type) {
            case chatGroupList: // Get the data to be shown in a list of groups.
                return GroupManager.instance.getListItemData();
            case messageList:   // Get the data to be shown in a room.
                return MessageManager.instance.getListItemData(item);
            case chatRoomList:          // Get the data to be show in a list of rooms.
                return RoomManager.instance.getListItemData(item.groupKey);
            case joinRoom:      // Get the candidate list of rooms and members.
                return JoinManager.instance.getListItemData(item);
            case selectGroupsAndRooms:
                return InvitationManager.instance.getListItemData();
            default:
                // TODO: log a message here.
                break;
        }

        // Return an empty list by default.  This should never happen.
        return new ArrayList<>();
    }

    /** Return a canonical change handler name for a given database model name. */
    public String getHandlerName(@NonNull final String base, @NonNull final String modelName) {
        return String.format(Locale.US, "%s{%s}", base, modelName);
    }

    /** Return a, possibly null, system resource. */
    public String getResource(final String key) {
        return mResourceMap.get(key);
    }

    /** Intialize the database manager by setting up localized resources. */
    public void init(final Context context) {
        mResourceMap.clear();
        mResourceMap.put(DEFAULT_ROOM_NAME_KEY, context.getString(R.string.DefaultRoomName));
        mResourceMap.put(SYSTEM_NAME_KEY, context.getString(R.string.app_name));
        mResourceMap.put(WELCOME_MESSAGE_KEY, context.getString(R.string.WelcomeMessageText));
    }

    /** Store an object on the database using a given path, pushKey, and properties. */
    public void updateChildren(final String path, final Map<String, Object> properties) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(path, properties);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
    }
}
