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
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.exp.Experience;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    // Public class methods.

    /** Return a canonical change handler name for a given database model name. */
    public static String getHandlerName(@NonNull final String base, @NonNull final String name) {
        return String.format(Locale.US, "%s{%s}", base, name);
    }

    /** Return a textual list of rooms (given by a new count map) flagging rooms with new items. */
    public static String getText(@NonNull final Map<String, Integer> roomCountMap) {
        // Process each room to determine if bolding is required to flag rooms with new items.
        StringBuilder textBuilder = new StringBuilder();
        for (String roomKey : roomCountMap.keySet()) {
            Room room = RoomManager.instance.getRoomProfile(roomKey);
            if (textBuilder.length() != 0)
                textBuilder.append(", ");
            if (roomCountMap.get(roomKey) > 0)
                textBuilder.append("<b>").append(room.getName()).append("</b>");
            else
                textBuilder.append(room.getName());
        }
        return textBuilder.toString();
    }

    public static int getUnseenExperienceCount(@NonNull final String groupKey,
                                               @NonNull final Map<String, Integer> map) {
        int result = 0;
        if (!ExperienceManager.instance.expGroupMap.containsKey(groupKey))
            return result;
        Set<Map.Entry<String, Map<String, Experience>>> expSet;
        expSet = ExperienceManager.instance.expGroupMap.get(groupKey).entrySet();
        for (Map.Entry<String, Map<String, Experience>> roomMap : expSet) {
            int roomNewCount = 0;
            for (Experience experience : roomMap.getValue().values())
                if (ExperienceManager.instance.isNew(experience))
                    roomNewCount++;
            map.put(roomMap.getKey(), roomNewCount);
            result += roomNewCount;
        }
        return result;
    }

    public static int getUnseenMessageCount(@NonNull final String groupKey,
                                            @NonNull final Map<String, Integer> map) {
        int result = 0;
        if (!GroupManager.instance.groupMap.containsKey(groupKey))
            return result;
        Set<Map.Entry<String, Map<String, Message>>> messageSet =
            MessageManager.instance.messageMap.get(groupKey).entrySet();
        for (Map.Entry<String, Map<String, Message>> roomMap : messageSet) {
            int roomNewCount = 0;
            for (Message message : roomMap.getValue().values())
                if (message.isUnseen())
                    roomNewCount++;
            map.put(roomMap.getKey(), roomNewCount);
            result += roomNewCount;
        }
        return result;
    }

    /** Store an object on the database using a given path, pushKey, and properties. */
    public static void updateChildren(final String path, final Map<String, Object> properties) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(path, properties);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
    }

    // Public instance methods.

    /** Return a, possibly null, system resource. */
    public String getResource(final String key) {
        return mResourceMap.get(key);
    }

    /** Initialize the database manager by setting up localized resources. */
    public void init(final Context context) {
        mResourceMap.clear();
        mResourceMap.put(DEFAULT_ROOM_NAME_KEY, context.getString(R.string.DefaultRoomName));
        mResourceMap.put(SYSTEM_NAME_KEY, context.getString(R.string.app_name));
        mResourceMap.put(WELCOME_MESSAGE_KEY, context.getString(R.string.WelcomeMessageText));
    }
}
