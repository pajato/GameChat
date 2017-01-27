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

package com.pajato.android.gamechat.database;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.adapter.DateHeaderItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.adapter.RoomItem;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.ProfileRoomChangeHandler;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.ProfileRoomChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.R.string.Group;

/**
 * Provide a class to manage database access to Room objects.
 *
 * @author Paul Michael Reilly
 */
public enum RoomManager {
    instance;

    // Public class constants.

    // Database paths, often used as format strings.

    /** The Firebase database path to the room objects. */
    public static final String ROOMS_PATH = GroupManager.GROUPS_PATH + "%s/rooms/";

    /** The Firebase database path to the room profile object. */
    public static final String ROOM_PROFILE_PATH = ROOMS_PATH + "%s/profile/";

    // Private class constants.

    /** The group profile change handler base name. */
    private static final String ROOM_PROFILE_LIST_CHANGE_HANDLER = "roomProfileChangeHandler";

    // Public instance variables.

    /** The collection of room profiles for the joined rooms, keyed by the room push key. */
    public Map<String, Room> roomMap = new HashMap<>();

    // Public instance methods.

    /** Return a room push key resulting from persisting the given room on the database. */
    public void createRoomProfile(final Room room) {
        // Ensure that the room is valid.  Abort if not, otherwise persist the room and set a
        // watcher on it.
        if (room.groupKey == null || room.key == null) return;
        String profilePath = String.format(Locale.US, ROOM_PROFILE_PATH, room.groupKey, room.key);
        room.createTime = new Date().getTime();
        DBUtils.instance.updateChildren(profilePath, room.toMap());
        setWatcher(room.groupKey, room.key);
    }

    /** Update the given room profile on the database. */
    public void updateRoomProfile(final Room room) {
        String path = String.format(Locale.US, ROOM_PROFILE_PATH, room.groupKey, room.key);
        room.modTime = new Date().getTime();
        DBUtils.instance.updateChildren(path, room.toMap());
    }

    /** Return the "Me" room or null if there is no such room for one reason or another. */
    public Room getMeRoom() {
        // Find the me room via the current account.
        String roomKey = AccountManager.instance.getMeRoomKey();
        return roomKey != null ? roomMap.get(roomKey) : null;
    }

    /** Return a room push key to use with a subsequent room object persistence. */
    public String getRoomKey(final String groupKey) {
        String roomsPath = String.format(Locale.US, ROOMS_PATH, groupKey);
        return FirebaseDatabase.getInstance().getReference().child(roomsPath).push().getKey();
    }

    /** Return a list of rooms for a specified group, optionally excluding the common room */
    public List<Room> getRooms(final String groupKey, final boolean includeCommonRoom) {
        List<Room> rooms = new ArrayList<>();
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        if (group == null) return rooms;
        for (String roomKey : group.roomList) {
            if (!includeCommonRoom && roomKey.equals(group.commonRoomKey)) {
                continue;
            }
            Room aRoom = RoomManager.instance.getRoomProfile(roomKey);
            rooms.add(aRoom);
        }
        return rooms;
    }

    /** Get the data as a set of room items for a given group key. */
    public List<ChatListItem> getListItemData(final String groupKey) {
        // Generate a list of items to render in the chat group list by extracting the items based
        // on the date header type ordering.
        Map<String, Map<String, Message>> roomMap;
        List<ChatListItem> result = new ArrayList<>();
        for (DateHeaderItem.DateHeaderType dht : DateHeaderItem.DateHeaderType.values()) {
            List<String> groupList = GroupManager.instance.getGroupList(dht);
            if (groupList != null && groupList.size() > 0 && groupList.contains(groupKey)) {
                // Add the header item followed by all the room items in the given group.
                result.add(new ChatListItem(new DateHeaderItem(dht)));
                roomMap = MessageManager.instance.messageMap.get(groupKey);
                for (String key : roomMap.keySet()) {
                    result.add(new ChatListItem(new RoomItem(groupKey, key)));
                }
            }
        }

        return result;
    }

    /** Obtain a name for the room with the given key, "Anonymous" if a name is not available. */
    public String getRoomName(final String roomKey) {
        Room room = roomMap.get(roomKey);
        return room != null ? room.name : "Anonymous";
    }

    /** Get the profile for a given room. */
    public Room getRoomProfile(final String roomKey) {
        return roomMap.get(roomKey);
    }

    /** Return the database path to the given group's profile. */
    public String getRoomProfilePath(final String groupKey, final String roomKey) {
        return String.format(Locale.US, ROOM_PROFILE_PATH, groupKey, roomKey);
    }

    /** Handle a account change event by setting up or clearing variables. */
    @Subscribe
    public void onAuthenticationChange(@NonNull final AuthenticationChangeEvent event) {
        // Determine if a User has been authenticated.  If so, do nothing, otherwise clear the
        // message list for the logged out User.
        if (event.account != null) return;
        roomMap.clear();
    }

    /** Handle a room profile change by updating the map. */
    @Subscribe public void onRoomProfileChange(@NonNull final ProfileRoomChangeEvent event) {
        roomMap.put(event.key, event.room);
    }

    /** Setup database listeners for the room profile and the experience profiles in the room. */
    public void setWatcher(final String groupKey, final String roomKey) {
        // Determine if the room has a profile change watcher.  If so, abort, if not, then set one.
        String path = RoomManager.instance.getRoomProfilePath(groupKey, roomKey);
        String name = DBUtils.instance.getHandlerName(ROOM_PROFILE_LIST_CHANGE_HANDLER, roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ProfileRoomChangeHandler(name, path, roomKey);
        DatabaseRegistrar.instance.registerHandler(handler);
        ExperienceManager.instance.setWatcher(groupKey, roomKey);
    }
}
