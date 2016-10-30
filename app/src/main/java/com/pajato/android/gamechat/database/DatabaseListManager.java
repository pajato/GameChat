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

import android.support.annotation.NonNull;

import com.pajato.android.gamechat.chat.ContactManager;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.adapter.ContactHeaderItem;
import com.pajato.android.gamechat.chat.adapter.DateHeaderItem;
import com.pajato.android.gamechat.chat.adapter.DateHeaderItem.DateHeaderType;
import com.pajato.android.gamechat.chat.adapter.GroupItem;
import com.pajato.android.gamechat.chat.adapter.MessageItem;
import com.pajato.android.gamechat.chat.adapter.RoomItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.ExpProfileListChangeHandler;
import com.pajato.android.gamechat.database.handler.ExperienceChangeHandler;
import com.pajato.android.gamechat.database.handler.JoinedRoomListChangeHandler;
import com.pajato.android.gamechat.database.handler.MessagesChangeHandler;
import com.pajato.android.gamechat.database.handler.ProfileGroupChangeHandler;
import com.pajato.android.gamechat.database.handler.ProfileRoomChangeHandler;
import com.pajato.android.gamechat.event.AccountStateChangeEvent;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.JoinedRoomListChangeEvent;
import com.pajato.android.gamechat.event.MessageChangeEvent;
import com.pajato.android.gamechat.event.MessageListChangeEvent;
import com.pajato.android.gamechat.event.ProfileGroupChangeEvent;
import com.pajato.android.gamechat.event.ProfileRoomChangeEvent;
import com.pajato.android.gamechat.game.Experience;
import com.pajato.android.gamechat.game.model.ExpProfile;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.adapter.ContactHeaderItem.ContactHeaderType.contacts;
import static com.pajato.android.gamechat.chat.adapter.ContactHeaderItem.ContactHeaderType.frequent;
import static com.pajato.android.gamechat.chat.adapter.DateHeaderItem.DateHeaderType.old;

/**
 * Provide a class to manage the app interactions with the database for lists of members,
 * chat messages, chat rooms, and chat groups.
 *
 * @author Paul Michael Reilly
 */
public enum DatabaseListManager {
    instance;

    // Public enums.

    /** The chat list type. */
    public enum ChatListType {
        group, message, room, member
    }

    // Public instance variables.

    /** The map associating group and room push keys with a map of experience profiles. */
    public Map<String, Map<String, Map<String, ExpProfile>>> expProfileMap = new HashMap<>();

    /** The map associating group and room push keys with a map of experience profiles. */
    public Map<String, Map<String, Map<String, Message>>> messageMap = new HashMap<>();

    /** The experience map. */
    public Map<String, Experience> experienceMap = new HashMap<>();

    /** The collection of profiles for the joined groups, keyed by the group push key. */
    public Map<String, Group> groupMap = new HashMap<>();

    /** The collection of profiles for the joined rooms, keyed by the room push key. */
    public Map<String, Room> roomMap = new HashMap<>();

    /** The current room (the one most previously selected or used.) */
    public Room currentRoom;

    // Private instance variables.

    /** A map associating date header type values with lists of group push keys. */
    private Map<DateHeaderType, List<String>> mDateHeaderTypeToGroupListMap = new HashMap<>();

    /** A map associating a group push key with it's most recent new message. */
    private Map<String, Message> mGroupToLastNewMessageMap = new HashMap<>();

    // Public instance methods.

    /** Return null, the current group key value or the me group. */
    public String getGroupKey() {
        Room room = currentRoom != null ? currentRoom : getMeRoom();
        return room != null ? room.groupKey : null;
    }

    /** Return a name for the group with the given key, "Anonymous" if a name is not available. */
    public String getGroupName(final String groupKey) {
        Group group = groupMap.get(groupKey);
        return group != null ? group.name : "Anonymous";
    }

    /** Get the profile for a given group, queueing it up to be loaded if necessary. */
    public Group getGroupProfile(final String groupKey) {
        // Return the group if it has been loaded.
        Group result = groupMap.get(groupKey);
        if (result != null) return result;

        // The given group needs to be loaded.  Do so now (checking that it has not already been
        // registered.)
        String path = DatabaseManager.instance.getGroupProfilePath(groupKey);
        String name = "profileChangeHandler" + groupKey;
        DatabaseEventHandler handler = DatabaseRegistrar.instance.getHandler(name);
        if (handler == null) handler = new ProfileGroupChangeHandler(name, path, groupKey);
        DatabaseRegistrar.instance.registerHandler(handler);

        return null;
    }

    /** Get a map of messages by room in a given group. */
    public Map<String, Map<String, Message>> getGroupMessages(final String groupKey) {
        return messageMap.get(groupKey);
    }

    /** Get the list data given a list type. */
    public List<ChatListItem> getList(final ChatListType type, final ChatListItem item) {
        switch (type) {
            case group:
                return getGroupListData();
            case message:
                return getMessageListData(item);
            case room:
                return getRoomListData(item.groupKey);
            case member:
                return getMemberListData(item);
            default:
                // TODO: log a message here.
                break;
        }

        // Return an empty list by default.  This should never happen.
        return new ArrayList<>();
    }

    /** Return the "Me" room or null if there is no such room for one reason or another. */
    public Room getMeRoom() {
        // Ensure that a search is viable.  Return null if not.
        if (roomMap == null || roomMap.size() == 0) return null;

        // Walk the list of rooms to find one (and there should only be one) with a Me room type.
        for (String key : roomMap.keySet()) {
            // Determine if this is a "me" room.
            Room room = roomMap.get(key);
            if (room.type == Room.ME) return room;
        }

        return null;
    }

    /** Return null, the current room key or the me room key. */
    public String getRoomKey() {
        Room room = currentRoom != null ? currentRoom : getMeRoom();
        return room != null ? room.key : null;
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

    /** Handle a authentication event. */
    @Subscribe public void onAccountStateChange(@NonNull final AccountStateChangeEvent event) {
        // Register an active rooms change handler on the account, if there is an account,
        // preferring a cached handler, if one is available.
        if (event.account != null) {
            // There is an active account.  Register a joined room list change handler.
            String path = DatabaseManager.instance.getJoinedRoomListPath(event.account);
            String name = "joinedRoomListChangeHandler";
            DatabaseEventHandler handler = DatabaseRegistrar.instance.getHandler(name);
            if (handler == null) handler = new JoinedRoomListChangeHandler(name, path);
            DatabaseRegistrar.instance.registerHandler(handler);
        } else {
            // Deal with either a logout or no valid user by clearing the various state.
            mDateHeaderTypeToGroupListMap.clear();
            messageMap.clear();
            groupMap.clear();
            mGroupToLastNewMessageMap.clear();
            roomMap.clear();
        }
    }

    /** Handle a room profile change by updating the map. */
    @Subscribe public void onGroupProfileChange(@NonNull final ProfileGroupChangeEvent event) {
        groupMap.put(event.key, event.group);
    }

    /** Handle changes to the list of joined rooms by capturing all group and room profiles. */
    @Subscribe public void onJoinedRoomsChange(@NonNull final JoinedRoomListChangeEvent event) {
        // Set up group and room profile value event listeners for the joined rooms.
        for (String entry : event.joinedRoomList) {
            // Deal with an invalid entry by continuing with the next entry.
            String[] split = entry.split(" ");
            if (split.length != 2) continue;

            // Setup listeners on the group and room profiles, the message, and the experience
            // profile changes, in that order.
            String groupKey = split[0];
            String roomKey = split[1];
            setGroupProfileWatcher(groupKey);
            setRoomProfileWatcher(groupKey, roomKey);
            setMessageWatcher(groupKey, roomKey);
        }
    }

    /** Handle a message change event by adding the message into the correct room list.  */
    @Subscribe public void onMessageChange(@NonNull final MessageChangeEvent event) {
        // Update the date headers for this message and post an event to trigger an adapter refresh.
        updateGroupHeaders(event.groupKey, event.message);
        AppEventManager.instance.post(new MessageListChangeEvent());
    }

    /** Handle a room profile change by updating the map. */
    @Subscribe public void onRoomProfileChange(@NonNull final ProfileRoomChangeEvent event) {
        roomMap.put(event.key, event.room);
    }

    /** Setup a listener for experience changes in the given room. */
    public void setExperienceWatcher(final ExpProfile profile) {
        // Set up a watcher in the given room for experiences changes.
        // Determine if a handle already exists. Abort if so.  Register a new handler if not.
        String name = String.format(Locale.US, "experiencesChangeHandler{%s}", profile.expKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ExperienceChangeHandler(name, profile);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    // Private instance methods.

    /** Return the date header type most closely associated with the given message timestamp. */
    private DateHeaderType getDateHeaderType(final Message message) {
        long now = new Date().getTime();
        for (DateHeaderType type : DateHeaderType.values()) {
            // Determine if this is the right dht value.
            if (now - message.createTime <= type.getLimit()) {
                // This is the correct dht value to use. Done.
                return type;
            }
        }
        return old;
    }

    /** Get the data as a set of list items for all groups. */
    private List<ChatListItem> getGroupListData() {
        // Generate a list of items to render in the chat group list by extracting the items based on
        // the date header type ordering.
        List<ChatListItem> result = new ArrayList<>();
        for (DateHeaderType dht : DateHeaderType.values()) {
            List<String> groupList = mDateHeaderTypeToGroupListMap.get(dht);
            if (groupList != null && groupList.size() > 0) {
                // Add the header item followed by all the group items.
                result.add(new ChatListItem(new DateHeaderItem(dht)));
                for (String groupKey : groupList) {
                    result.add(new ChatListItem(new GroupItem(groupKey)));
                }
            }
        }

        return result;
    }

    /** Return a list of ordered chat items from a map of chronologically ordered messages. */
    private List<ChatListItem> getItems(final ChatListItem item,
                                        final Map<DateHeaderType, List<Message>> messageMap) {
        // Build the list of display items, in reverse order (oldest to newest).
        List<ChatListItem> result = new ArrayList<>();
        String groupKey = item.groupKey;
        String roomKey = item.roomKey;
        DateHeaderType[] types = DateHeaderType.values();
        int size = types.length;
        for (int index = size - 1; index >= 0; index--) {
            // Add the header item followed by all the room messages.
            DateHeaderType dht = types[index];
            List<Message> list = messageMap.get(dht);
            if (list != null) {
                result.add(new ChatListItem(new DateHeaderItem(dht)));
                for (Message message : list) {
                    result.add(new ChatListItem(new MessageItem(groupKey, roomKey, message)));
                }
            }
        }

        return result;
    }

    /** Return a set of potential group members. */
    private List<ChatListItem> getMemberListData(final ChatListItem item) {
        // Determine if there are any frequent members to add.  If so, add them and then add the
        // members from the User's device contacts.
        List<ChatListItem> result = new ArrayList<>();
        if (item != null) {
            // TODO: extract the list of frequent members from the item, somehow and add it to the
            // result.
            result.add(new ChatListItem(new ContactHeaderItem(frequent)));
        }
        result.add(new ChatListItem(new ContactHeaderItem(contacts)));
        result.addAll(ContactManager.instance.getDeviceContactList());

        return result;
    }

    /** Return a list of messages, an empty list if there are none to be had, for a given item. */
    private List<ChatListItem> getMessageListData(final ChatListItem item) {
        // Generate a map of date header types to a list of messages, i.e. a chronological ordering
        // of the messages.
        String groupKey = item.groupKey;
        String roomKey = item.roomKey;
        return getItems(item, getMessageMap(getGroupMessages(groupKey).get(roomKey)));
    }

    /** Return a map of the given messages, sorted into chronological buckets. */
    private Map<DateHeaderType, List<Message>> getMessageMap(final Map<String, Message> messageList) {
        // Stick the messages into a message map keyed by date header type.
        Map<DateHeaderType, List<Message>> result = new HashMap<>();
        for (Message message : messageList.values()) {
            // Append the message to the list keyed by the date header type value associated with
            // the message creation date.
            DateHeaderType type = getDateHeaderType(message);
            List<Message> list = result.get(type);
            if (list == null) {
                list = new ArrayList<>();
                result.put(type, list);
            }
            list.add(message);
        }

        return result;
    }

    /** Get the data as a set of room items for a given group key. */
    private List<ChatListItem> getRoomListData(final String groupKey) {
        // Generate a list of items to render in the chat group list by extracting the items based
        // on the date header type ordering.
        List<ChatListItem> result = new ArrayList<>();
        for (DateHeaderType dht : DateHeaderType.values()) {
            List<String> groupList = mDateHeaderTypeToGroupListMap.get(dht);
            if (groupList != null && groupList.size() > 0 && groupList.contains(groupKey)) {
                // Add the header item followed by all the room items in the given group.
                result.add(new ChatListItem(new DateHeaderItem(dht)));
                Map<String, Map<String, Message>> roomMap = messageMap.get(groupKey);
                for (String key : roomMap.keySet()) {
                    result.add(new ChatListItem(new RoomItem(groupKey, key)));
                }
            }
        }

        return result;
    }

    // Package private instance methods.

    // Private instance methods.

    /** Setup a listener for experience changes in the given room. */
    private void setExpProfileListWatcher(final String groupKey, final String roomKey) {
        // Obtain a room and set watchers on all the experience profiles in that room.
        // Determine if a handle already exists. Abort if so.  Register a new handler if not.
        String name = String.format(Locale.US, "expProfileListChangeHandler{%s}", roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ExpProfileListChangeHandler(name, groupKey, roomKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Setup a database listener for the group profile. */
    private void setGroupProfileWatcher(final String groupKey) {
        // Kick off a value event listener for the group profile..
        String path = DatabaseManager.instance.getGroupProfilePath(groupKey);
        String name = "profileChangeHandler" + groupKey;
        DatabaseEventHandler handler = DatabaseRegistrar.instance.getHandler(name);
        if (handler == null) handler = new ProfileGroupChangeHandler(name, path, groupKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Setup a Firebase child event listener for the messages in the given joined room. */
    private void setMessageWatcher(final String groupKey, final String roomKey) {
        // There is an active account.  Register it.
        String name = String.format(Locale.US, "messagesChangeHandler%s|%s", groupKey, roomKey);
        DatabaseEventHandler handler = DatabaseRegistrar.instance.getHandler(name);
        if (handler == null) handler = new MessagesChangeHandler(name, groupKey, roomKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Setup database listeners for the room profile and the experience profiles in the room. */
    private void setRoomProfileWatcher(final String groupKey, final String roomKey) {
        // Kick off a value event listener for the room profile.
        String path = DatabaseManager.instance.getRoomProfilePath(groupKey, roomKey);
        String name = "profileChangeHandler" + roomKey;
        DatabaseEventHandler handler = new ProfileRoomChangeHandler(name, path, roomKey);
        DatabaseRegistrar.instance.registerHandler(handler);
        setExpProfileListWatcher(groupKey, roomKey);
    }

    /** Update the headers used to bracket the messages in the main list. */
    private void updateGroupHeaders(final String groupKey, final Message message) {
        // Add the new message to be the last message eminating from
        // the given group.  The rebuild the lists of date header type to group list associations.
        mGroupToLastNewMessageMap.put(groupKey, message);
        mDateHeaderTypeToGroupListMap.clear();
        long nowTimestamp = new Date().getTime();
        for (String key : mGroupToLastNewMessageMap.keySet()) {
            // Determine which date header type the current group should be associated with.
            long groupTimestamp = message.createTime;
            for (DateHeaderType dht : DateHeaderType.values()) {
                // Determine if the current group fits the constraints of the current date header
                // type.  The declaration of DateHeaderType is ordered so that this algorithm will
                // work.
                if (dht == old || nowTimestamp - groupTimestamp <= dht.getLimit()) {
                    // This is the one.  Add this group to the associated list.
                    List<String> list = mDateHeaderTypeToGroupListMap.get(dht);
                    if (list == null) {
                        list = new ArrayList<>();
                        mDateHeaderTypeToGroupListMap.put(dht, list);
                    }
                    list.add(key);
                    break;
                }
            }
        }
    }

}
