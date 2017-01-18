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

    // Public enums.

    /** The chat list type. */
    public enum ChatListType {
        addGroup(R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp),
        addRoom(R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp,
                R.string.CreateRoomMenuTitle),
        group,
        message(R.drawable.ic_more_vert_white_24dp, R.drawable.ic_arrow_back_white_24dp),
        room(R.drawable.ic_more_vert_white_24dp, R.drawable.ic_arrow_back_white_24dp),
        joinMemberRoom(R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp),
        joinRoom(R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp,
                 R.string.JoinRoomsMenuTitle);

        /** The overflow menu icon resource id. */
        public int overflowMenuIconResourceId;

        /** The overflow menu resource id. */
        public int overflowMenuResourceId;

        /** The navigation icon resource id. */
        public int navigationIconResourceId;

        /** The toolbar title resource id. */
        public int titleResourceId;

        /** Build the default (group) instance. */
        ChatListType() {}

        /** Build an instance using the given arguments. */
        ChatListType(final int overflowMenuIconResourceId, final int navigationIconResourceId) {
            this.overflowMenuIconResourceId = overflowMenuIconResourceId;
            this.navigationIconResourceId = navigationIconResourceId;
            overflowMenuResourceId = R.menu.overflow_main_menu;
        }

        /** Build an instance using all possible arguments. */
        ChatListType(final int overflowResId, final int navResId, final int titleResId) {
            this(overflowResId, navResId);
            titleResourceId = titleResId;
        }
    }

    // Public class constants.

    // Lookup keys.
    public static final String DEFAULT_ROOM_NAME_KEY = "defaultRoomNameKey";
    public static final String SYSTEM_NAME_KEY = "systemNameKey";
    public static final String WELCOME_MESSAGE_KEY = "welcomeMessageKey";

    // Public instance variables.

    // Private instance variables.

    /** The map providing localized resources, setup during initialization. */
    private Map<String, String> mResourceMap = new HashMap<>();

    // Public instance methods.

    /** Get the list data given a list type. */
    public List<ChatListItem> getList(@NonNull final ChatListType type, final ChatListItem item) {
        switch (type) {
            case group:
                return GroupManager.instance.getGroupListData();
            case message:
                return MessageManager.instance.getMessageListData(item);
            case room:
                return RoomManager.instance.getRoomListData(item.groupKey);
            case joinRoom:
                return JoinManager.instance.getJoinableRoomsListData(item);
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

    /** Return a, poissibly null, system resource. */
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
