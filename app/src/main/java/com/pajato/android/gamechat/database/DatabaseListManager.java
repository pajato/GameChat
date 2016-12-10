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

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.adapter.DateHeaderItem;
import com.pajato.android.gamechat.chat.adapter.DateHeaderItem.DateHeaderType;
import com.pajato.android.gamechat.chat.adapter.GroupItem;
import com.pajato.android.gamechat.chat.adapter.MessageItem;
import com.pajato.android.gamechat.chat.adapter.RoomItem;
import com.pajato.android.gamechat.chat.adapter.RoomsHeaderItem;
import com.pajato.android.gamechat.chat.adapter.SelectableMemberItem;
import com.pajato.android.gamechat.chat.adapter.SelectableRoomItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.ExpProfileListChangeHandler;
import com.pajato.android.gamechat.database.handler.ExperienceChangeHandler;
import com.pajato.android.gamechat.database.handler.MemberChangeHandler;
import com.pajato.android.gamechat.database.handler.MessageListChangeHandler;
import com.pajato.android.gamechat.database.handler.ProfileGroupChangeHandler;
import com.pajato.android.gamechat.database.handler.ProfileRoomChangeHandler;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.MemberChangeEvent;
import com.pajato.android.gamechat.event.MessageChangeEvent;
import com.pajato.android.gamechat.event.ProfileGroupChangeEvent;
import com.pajato.android.gamechat.event.ProfileRoomChangeEvent;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.model.ExpProfile;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        group, message, room, joinMemberRoom, joinRoom,
    }

    // Private class constants.

    /** The experience profile change handler base name. */
    private static final String EXP_PROFILE_LIST_CHANGE_HANDLER = "expProfileListChangeHandler";

    /** The experience change handler base name. */
    private static final String EXPERIENCES_CHANGE_HANDLER = "experiencesChangeHandler";

    /** The group member change handler base name. */
    private static final String GROUP_MEMBER_CHANGE_HANDLER = "groupMemberChangeHandler";

    /** The group profile change handler base name. */
    private static final String GROUP_PROFILE_CHANGE_HANDLER = "groupProfileChangeHandler";

    /** The message list change handler base name. */
    private static final String MESSAGE_LIST_CHANGE_HANDLER = "messageListChangeHandler";

    /** The group profile change handler base name. */
    private static final String ROOM_PROFILE_LIST_CHANGE_HANDLER = "roomProfileChangeHandler";

    // Public instance variables.

    /** The map associating group and room push keys with a map of experience profiles. */
    public Map<String, Map<String, Map<String, ExpProfile>>> expProfileMap = new HashMap<>();

    /** The experience map. */
    public Map<String, Experience> experienceMap = new HashMap<>();

    /** The collection of profiles for the joined groups, keyed by the group push key. */
    public Map<String, Group> groupMap = new HashMap<>();

    /** The map associating a group with the members in that group. */
    public Map<String, Map<String, Account>> groupMemberMap = new HashMap<>();

    /** The map associating group and room push keys with a map of experience profiles. */
    public Map<String, Map<String, Map<String, Message>>> messageMap = new HashMap<>();

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

    /** Return null or a group member using the current account holder's id and given group key. */
    public Account getGroupMember(@NonNull final String groupKey) {
        // Determine if there is no curent account (should be impossible.)  Abort if so.
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null) return null;

        // Determine if there is an expected member account in the given group.  Abort if not.
        // Return the account if so.
        Map<String, Account> memberMap = groupMemberMap.get(groupKey);
        if (memberMap == null) return null;
        return memberMap.get(account.id);
    }

    /** Return null or a group member using the current account holder's id and given group key. */
    public Account getGroupMember(@NonNull final String groupKey, @NonNull final String memberKey) {
        // Determine if there is an expected member account in the given group.  Abort if not.
        // Return the account if so.
        Map<String, Account> memberMap = groupMemberMap.get(groupKey);
        if (memberMap == null) return null;
        return memberMap.get(memberKey);
    }

    /** Return a name for the group with the given key, "Anonymous" if a name is not available. */
    public String getGroupName(final String groupKey) {
        Group group = groupMap.get(groupKey);
        return group != null ? group.name : "Anonymous";
    }

    /** Get the profile for a given group, queueing it up to be loaded if necessary. */
    public Group getGroupProfile(@NonNull final String groupKey) {
        // Return the group if it has been loaded.  Set a watcher to load it if not.
        Group result = groupMap.get(groupKey);
        if (result != null) return result;
        setGroupProfileWatcher(groupKey);
        return null;
    }

    /** Get a map of messages by room in a given group. */
    public Map<String, Map<String, Message>> getGroupMessages(final String groupKey) {
        return messageMap.get(groupKey);
    }

    /** Get the list data given a list type. */
    public List<ChatListItem> getList(@NonNull final ChatListType type, final ChatListItem item) {
        switch (type) {
            case group: return getGroupListData();
            case message: return getMessageListData(item);
            case room: return getRoomListData(item.groupKey);
            case joinRoom: return getJoinableRoomsListData(item);
            default:
                // TODO: log a message here.
                break;
        }

        // Return an empty list by default.  This should never happen.
        return new ArrayList<>();
    }

    /** Return the "Me" room or null if there is no such room for one reason or another. */
    public Room getMeRoom() {
        // Ensure that a search is viable.  Return null if not, otherwise walk the list of rooms to
        // find one (and only one) with a "Me" room.
        if (roomMap == null || roomMap.size() == 0) return null;
        for (Room room : roomMap.values()) if (room.type == Room.ME) return room;
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

    /** Handle a account change event by setting up or tearing down watchers. */
    @Subscribe public void onAuthenticationChange(@NonNull final AuthenticationChangeEvent event) {
        // Determine if a User has been authenticated.
        if (event.account != null) {
            // There is an active account.  Watch for changes to the profiles for each
            // group in the account.
            for (String groupKey : event.account.joinList) setGroupProfileWatcher(groupKey);
        } else {
            // Deal with either a logout or no valid user by clearing the various state.
            mDateHeaderTypeToGroupListMap.clear();
            messageMap.clear();
            groupMap.clear();
            mGroupToLastNewMessageMap.clear();
            roomMap.clear();
            groupMemberMap.clear();
        }
    }

    /** Handle a room profile change by updating the map. */
    @Subscribe public void onGroupProfileChange(@NonNull final ProfileGroupChangeEvent event) {
        // Ensure that the group profile is cached and set up watchers for each member and room in
        // the group.
        groupMap.put(event.key, event.group);
        for (String key : event.group.roomList) setRoomProfileWatcher(event.group.key, key);
        for (String key : event.group.memberList) setMemberWatcher(event.group.key, key);
    }

    /** Handle changes to the list of joined rooms by capturing all group and room profiles. */
    @Subscribe public void onMemberChange(@NonNull final MemberChangeEvent event) {
        // Update the member in the local caches, creating the caches as necessary.
        Map<String, Account> map = groupMemberMap.get(event.member.groupKey);
        if (map == null) map = new HashMap<>();
        map.put(event.member.id, event.member);
        groupMemberMap.put(event.member.groupKey, map);

        // Determine if the payload is for the current account holder.  If so, set a message watcher
        // on the joined rooms.
        if (event.member.id.equals(AccountManager.instance.getCurrentAccountId()))
            for (String roomKey : event.member.joinList)
                setMessageWatcher(event.member.groupKey, roomKey);
    }

    /** Handle a message change event by adding the message into the correct room list.  */
    @Subscribe public void onMessageListChange(@NonNull final MessageChangeEvent event) {
        // Update the date headers for this message and post an event to trigger an adapter refresh.
        updateGroupHeaders(event.message);
        AppEventManager.instance.post(new ChatListChangeEvent());
    }

    /** Handle a room profile change by updating the map. */
    @Subscribe public void onRoomProfileChange(@NonNull final ProfileRoomChangeEvent event) {
        roomMap.put(event.key, event.room);
    }

    /** Setup a listener for experience changes in the given room. */
    public void setExperienceWatcher(final ExpProfile profile) {
        // Set up a watcher in the given room for experiences changes.
        // Determine if a handle already exists. Abort if so.  Register a new handler if not.
        String name = getHandlerName(EXPERIENCES_CHANGE_HANDLER, profile.expKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ExperienceChangeHandler(name, profile);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    // Private instance methods.

    /** Return a list of available room entries from the given group excluding joined ones. */
    private List<ChatListItem> getAvailableMembers(final ChatListItem item) {
        // Get a list of all members visible to the current User.
        List<ChatListItem> result = new ArrayList<>();
        List<ChatListItem> items = new ArrayList<>();
        List<String> groupList = getGroups(item);
        for (String groupKey : groupList) {
            Map<String, Account> map = groupMemberMap.get(groupKey);
            for (Account member : map.values()) {
                if (member.id.equals(AccountManager.instance.getCurrentAccountId())) continue;
                items.add(new ChatListItem(new SelectableMemberItem(groupKey, member)));
            }
        }

        // Generate a header for the members section based on the absence or presence of items.
        int noAvailableMembers = R.string.MembersNotAvailableHeaderText;
        int availableMembers = R.string.MembersAvailableHeaderText;
        int resourceId = items.size() == 0 ? noAvailableMembers : availableMembers;
        result.add(new ChatListItem(new RoomsHeaderItem(resourceId)));
        result.addAll(items);
        return result;
    }

    /** Return a possibly empty list of items consisting of unjoined rooms. */
    private List<ChatListItem> getAvailableRooms(final ChatListItem item) {
        // Determine if there are groups to look at.  If not, return an empty result.
        List<ChatListItem> result = new ArrayList<>();
        List<String> groupList = getGroups(item);

        // The group list is not empty.  Determine if there are any joinable rooms.
        List<String> joinedRoomList = getJoinedRooms(groupList);
        List<String> joinableRoomList = getJoinableRooms(groupList, joinedRoomList);
        if (joinableRoomList.size() > 0) {
            // There are joinable rooms.  Add a header item and the list of joinable rooms.
            result.add(new ChatListItem(new RoomsHeaderItem(R.string.RoomsAvailableHeaderText)));
            for (String roomKey : joinableRoomList) {
                Room room = roomMap.get(roomKey);
                result.add(new ChatListItem(new SelectableRoomItem(room.groupKey, roomKey)));
            }
        }

        return result;
    }

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
        // Generate a list of items to render in the chat group list by extracting the items based
        // on the date header type ordering.
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

        // Add the private rooms.

        return result;
    }

    /** Return a list of joined group push keys based on the given item. */
    private List<String> getGroups(final ChatListItem item) {
        List<String> result = new ArrayList<>();
        Account account = AccountManager.instance.getCurrentAccount();
        if (item != null && item.groupKey != null) result.add(item.groupKey);
        if (result.isEmpty() && account != null) result.addAll(account.joinList);
        return result;
    }

    /** Return a canonical change handler name for a given database model name. */
    private String getHandlerName(@NonNull final String base, @NonNull final String modelName) {
        return String.format(Locale.US, "%s{%s}", base, modelName);
    }

    /** Return a list of ordered chat items from a map of chronologically ordered messages. */
    private List<ChatListItem> getItems(final Map<DateHeaderType, List<Message>> messageMap) {
        // Build the list of display items, in reverse order (oldest to newest).
        List<ChatListItem> result = new ArrayList<>();
        DateHeaderType[] types = DateHeaderType.values();
        int size = types.length;
        for (int index = size - 1; index >= 0; index--) {
            // Add the header item followed by all the room messages.
            DateHeaderType dht = types[index];
            List<Message> list = messageMap.get(dht);
            if (list != null) {
                result.add(new ChatListItem(new DateHeaderItem(dht)));
                for (Message message : list) {
                    result.add(new ChatListItem(new MessageItem(message)));
                }
            }
        }

        return result;
    }

    /** Return a set of room the current user might choose to join. */
    private List<ChatListItem> getJoinableRoomsListData(final ChatListItem item) {
        // Determine if there are any rooms to present (excludes joined rooms).
        List<ChatListItem> result = new ArrayList<>();
        result.addAll(getAvailableRooms(item));
        result.addAll(getAvailableMembers(item));
        if (result.size() > 0) return result;

        // There are no rooms to join.  Provide a header message to that effect.
        result.add(new ChatListItem(new RoomsHeaderItem(R.string.NoJoinableRoomsHeaderText)));
        return result;
    }

    /** Return a list of joinable rooms: those that are public and not already joined. */
    private List<String> getJoinableRooms(@NonNull final List<String> groupList,
                                          @NonNull final List<String> joinedRoomList) {
        List<String> result = new ArrayList<>();
        for (String groupKey : groupList)
            // For each group, include each unjoined, public room in the result.
            for (String roomKey : getRoomList(groupKey)) {
                // Determine if this room is joined.  If so, continue.
                if (joinedRoomList.contains(roomKey)) continue;

                // Determine if this room is public.  If so, accumulate it to the result.
                Room room = roomMap.get(roomKey);
                if (room.type == Room.PUBLIC) result.add(roomKey);
            }
        return result;
    }

    /** Return a list of joined room push keys for the current User. */
    private List<String> getJoinedRooms(final List<String> groups) {
        List<String> result = new ArrayList<>();
        for (String groupKey : groups) {
            Map<String, Account> map = groupMemberMap.get(groupKey);
            String id = AccountManager.instance.getCurrentAccountId();
            Account member = map != null && id != null ? map.get(id) : null;
            if (member != null) result.addAll(member.joinList);
        }
        return result;
    }

    /** Return a list of messages, an empty list if there are none to be had, for a given item. */
    private List<ChatListItem> getMessageListData(@NonNull final ChatListItem item) {
        // Generate a map of date header types to a list of messages, i.e. a chronological ordering
        // of the messages.
        String groupKey = item.groupKey;
        String roomKey = item.key;
        return getItems(getMessageMap(getGroupMessages(groupKey).get(roomKey)));
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

    /** Return a list of candidate rooms in a given group. */
    private List<String> getRoomList(@NonNull final String groupKey) {
        List<String> result = new ArrayList<>();
        Group group = getGroupProfile(groupKey);
        if (group != null) result.addAll(group.roomList);
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

    /** Setup a listener for experience changes in the given room. */
    private void setExpProfileListWatcher(final String groupKey, final String roomKey) {
        // Obtain a room and set watchers on all the experience profiles in that room.
        // Determine if a handle already exists. Abort if so.  Register a new handler if not.
        String name = getHandlerName(EXP_PROFILE_LIST_CHANGE_HANDLER, roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ExpProfileListChangeHandler(name, groupKey, roomKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Setup a database listener for the group profile. */
    private void setGroupProfileWatcher(final String groupKey) {
        // Determine if the group has a profile change watcher.  If so, abort, if not, then set one.
        String path = DatabaseManager.instance.getGroupProfilePath(groupKey);
        String name = getHandlerName(GROUP_PROFILE_CHANGE_HANDLER, groupKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ProfileGroupChangeHandler(name, path, groupKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Setup a listener for member changes in the given account. */
    private void setMemberWatcher(final String groupKey, final String memberKey) {
        // Obtain a room and set watchers on all the experience profiles in that room.
        // Determine if a handle already exists. Abort if so.  Register a new handler if not.
        String tag = String.format(Locale.US, "%s,%s", groupKey, memberKey);
        String name = getHandlerName(GROUP_MEMBER_CHANGE_HANDLER, tag);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        String path = DatabaseManager.instance.getGroupMembersPath(groupKey, memberKey);
        DatabaseEventHandler handler;
        handler = new MemberChangeHandler(name, path, memberKey, groupKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Setup a Firebase child event listener for the messages in the given joined room. */
    private void setMessageWatcher(final String groupKey, final String roomKey) {
        // There is an active account.  Register it.
        String name = getHandlerName(MESSAGE_LIST_CHANGE_HANDLER, roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new MessageListChangeHandler(name, groupKey, roomKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Setup database listeners for the room profile and the experience profiles in the room. */
    private void setRoomProfileWatcher(final String groupKey, final String roomKey) {
        // Determine if the room has a profile change watcher.  If so, abort, if not, then set one.
        String path = DatabaseManager.instance.getRoomProfilePath(groupKey, roomKey);
        String name = getHandlerName(ROOM_PROFILE_LIST_CHANGE_HANDLER, roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ProfileRoomChangeHandler(name, path, roomKey);
        DatabaseRegistrar.instance.registerHandler(handler);
        setExpProfileListWatcher(groupKey, roomKey);
    }

    /** Update the headers used to bracket the messages in the main list. */
    private void updateGroupHeaders(final Message message) {
        // Add the new message to be the last message eminating from
        // the given group.  The rebuild the lists of date header type to group list associations.
        mGroupToLastNewMessageMap.put(message.groupKey, message);
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
