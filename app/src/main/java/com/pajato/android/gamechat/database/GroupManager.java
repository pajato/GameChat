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

import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.ProfileGroupChangeHandler;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.MessageChangeEvent;
import com.pajato.android.gamechat.event.ProfileGroupChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.common.adapter.ListItem.DateHeaderType.old;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.chatGroup;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.chatRoom;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.date;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.resourceHeader;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public enum GroupManager {
    instance;

    // Public class constants.

    // Database paths, often used as format strings.
    public static final String GROUPS_PATH = "/groups/";
    public static final String GROUP_PROFILE_PATH = GROUPS_PATH + "%s/profile/";

    // Private class constants.

    /** The group profile change handler base name. */
    private static final String GROUP_PROFILE_CHANGE_HANDLER = "groupProfileChangeHandler";

    // Public instance variables.

    /** The collection of profiles for the joined groups, keyed by the group push key. */
    public Map<String, Group> groupMap = new HashMap<>();

    // Private instance variables.

    /** A map associating date header type values with lists of group push keys. */
    private Map<ListItem.DateHeaderType, List<String>> mDateHeaderTypeToGroupListMap =
            new HashMap<>();

    /** A map associating a group push key with it's most recent new message. */
    private Map<String, Message> mGroupToLastNewMessageMap = new HashMap<>();

    // Public instance methods.

    /** Persist a given group object using the given key. */
    public void createGroupProfile(final Group group) {
        // Ensure that a valid group key exists.  Abort quietly (for now) if not.
        if (group.key == null) return;
        String profilePath = String.format(Locale.US, GROUP_PROFILE_PATH, group.key);
        group.createTime = new Date().getTime();
        DBUtils.updateChildren(profilePath, group.toMap());
        setWatcher(group.key);
    }

    /** Return a room push key to use with a subsequent room object persistence. */
    public String getGroupKey() {
        return FirebaseDatabase.getInstance().getReference().child(GROUPS_PATH).push().getKey();
    }

    /** Return a list of group push keys associated with a given date header type. */
    public List<String> getGroupList(final ListItem.DateHeaderType type) {
        return mDateHeaderTypeToGroupListMap.get(type);
    }

    /** Get a map of messages by room in a given group. */
    public Map<String, Map<String, Message>> getGroupMessages(final String groupKey) {
        return MessageManager.instance.messageMap.get(groupKey);
    }

    /** Return a name for the group with the given key, "Anonymous" if a name is not available. */
    public String getGroupName(final String groupKey) {
        Group group = groupMap.get(groupKey);
        return group != null ? group.name : null;
    }

    /** Get the profile for a given group, queueing it up to be loaded if necessary. */
    public Group getGroupProfile(@NonNull final String groupKey) {
        // Return the group if it has been loaded.  Set a watcher to load it if not.
        Group result = groupMap.get(groupKey);
        if (result != null) return result;
        setWatcher(groupKey);
        return null;
    }

    /** Return the database path to the given group's profile. */
    public String getGroupProfilePath(final String groupKey) {
        return String.format(Locale.US, GROUP_PROFILE_PATH, groupKey);
    }

    /** Return a list of joined group push keys based on the given item. */
    public List<String> getGroups(final ListItem item) {
        List<String> result = new ArrayList<>();
        Account account = AccountManager.instance.getCurrentAccount();
        if (item != null && item.groupKey != null)
            result.add(item.groupKey);
        if (result.isEmpty() && account != null)
            result.addAll(account.joinList);
        return result;
    }

    /** Get the data as a set of list items for all groups. */
    public List<ListItem> getListItemData() {
        // Determine whether to handle no groups (a set of welcome list items), one group (a set of
        // group rooms) or more than one group (a set of groups).
        switch (groupMap.size()) {
            case 0:
                return getNoGroupsItemList();
            case 1:
                //return getGroupRoomsItemList();
            default:
                return getGroupsItemList();
        }
    }

    /** Handle a account change event by setting up or clearing variables. */
    @Subscribe public void onAuthenticationChange(@NonNull final AuthenticationChangeEvent event) {
        // Determine if a User has been authenticated.  If so, setup the group profile change
        // watchers on all groups the User has joined.
        if (event.account != null) {
            // Set up watchers on the User's private group profile and all groups the User has
            // joined.
            if (event.account.groupKey != null)
                setWatcher(event.account.groupKey);
            for (String groupKey : event.account.joinList)
                setWatcher(groupKey);
            return;
        }

        // There is no current User.  Clear the cached data, if any.
        mDateHeaderTypeToGroupListMap.clear();
        groupMap.clear();
        mGroupToLastNewMessageMap.clear();
    }

    /** Handle a joined group profile change by updating the map and ensuring watchers are set. */
    @Subscribe public void onGroupProfileChange(@NonNull final ProfileGroupChangeEvent event) {
        // Ensure that the group profile key and the group exist.  Abort if not, otherwise set
        // watchers and cache all but the me group.  The me group is handled by the account manager.
        String groupKey = event.key;
        if (groupKey == null || event.group == null)
            return;
        for (String key : event.group.memberList)
            MemberManager.instance.setWatcher(groupKey, key);
        if (!groupKey.equals(AccountManager.instance.getMeGroupKey())) {
            groupMap.put(event.key, event.group);
            // if this isn't the me group, add watchers for all the rooms
            for (String roomKey : event.group.roomList) {
                RoomManager.instance.setWatcher(groupKey, roomKey);
            }
        }
    }

    /** Handle a message change event by adding the message into the correct room list.  */
    @Subscribe public void onMessageListChange(@NonNull final MessageChangeEvent event) {
        // Update the date headers for this message and post an event to trigger an adapter refresh.
        updateGroupHeaders(event.message);
        AppEventManager.instance.post(new ChatListChangeEvent());
    }

    /** Setup a database listener for the group profile. */
    public void setWatcher(final String groupKey) {
        // Determine if the group has a profile change watcher.  If so, abort, if not, then set one.
        String path = GroupManager.instance.getGroupProfilePath(groupKey);
        String name = DBUtils.getHandlerName(GROUP_PROFILE_CHANGE_HANDLER, groupKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ProfileGroupChangeHandler(name, path, groupKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Update the given group profile on the database. */
    public void updateGroupProfile(final Group group) {
        String path = String.format(Locale.US, GROUP_PROFILE_PATH, group.key);
        group.modTime = new Date().getTime();
        DBUtils.updateChildren(path, group.toMap());
    }

    // Private instance methods.

    /** Add a group list item for the given kind (chat message or game experience) and group. */
    private void addItem(@NonNull final List<ListItem> result, @NonNull final String groupKey) {
        String name = getGroupName(groupKey);
        Map<String, Integer> roomCountMap = new HashMap<>();
        int count = getNewMessageCount(groupKey, roomCountMap);
        String text = getGroupText(roomCountMap);
        result.add(new ListItem(chatGroup, groupKey, null, name, count, text));
    }

    /** Return a list of chat group or room items. */
    private List<ListItem> getGroupsItemList() {
        // Generate a list of items to render in the chat group list by extracting the items based
        // on the date header type ordering.
        List<ListItem> result = new ArrayList<>();
        for (ListItem.DateHeaderType dht : ListItem.DateHeaderType.values()) {
            List<String> groupList = mDateHeaderTypeToGroupListMap.get(dht);
            if (groupList != null && groupList.size() > 0) {
                // Add the header item followed by all the group items.
                result.add(new ListItem(date, dht.resId));
                for (String groupKey : groupList)
                    if(!(groupKey.equals(AccountManager.instance.getMeGroupKey())))
                        addItem(result, groupKey);
            }
        }
        return result;
    }

    /** Return a textual list of rooms in the group indicating new items by bolding the name. */
    private String getGroupText(@NonNull final Map<String, Integer> roomCountMap) {
        // Process each room to determine if bolding is required.
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

    /** Return 0 or the number of unseen messages in all the joined rooms for the given group. */
    private int getNewMessageCount(@NonNull final String key,
                                   @NonNull final Map<String, Integer> roomCountMap) {
        // Return the total number of unseen messages in the joined rooms for the given group.
        int result = 0;
        Map<String, Map<String, Message>> map = GroupManager.instance.getGroupMessages(key);
        String accountId = AccountManager.instance.getCurrentAccountId();
        if (map == null || accountId == null)
            return result;
        for (String roomKey : map.keySet()) {
            int roomNewCount = 0;
            for (Message message : map.get(roomKey).values())
                if (message.unseenList != null && message.unseenList.contains(accountId))
                    roomNewCount++;
            roomCountMap.put(roomKey, roomNewCount);
            result += roomNewCount;
        }
        return result;
    }

    /** Return the normal case: more than one group. */
    private List<ListItem> getNoGroupsItemList() {
        // Determine if the me room exists.  If not, about with an empty list, otherwise return a
        // list of items from the me room.
        List<ListItem> result = new ArrayList<>();
        result.add(new ListItem(resourceHeader, R.string.NoGroupsHeaderText));
        Room room = RoomManager.instance.getMeRoom();
        if (room == null)
            return result;

        // Collect and return a list containing a single list item from the me room.
        Map<String, Integer> unseenCountMap = new HashMap<>();
        int count = DBUtils.getUnseenMessageCount(room.groupKey, unseenCountMap);
        String text = DBUtils.getText(unseenCountMap);
        result.add(new ListItem(chatRoom, room.groupKey, room.key, room.name, count, text));
        return result;
    }

    /** Update the headers used to bracket the messages in the main list. */
    private void updateGroupHeaders(final Message message) {
        // Filter out me room messages.
        Account account = AccountManager.instance.getCurrentAccount();
        if (message.groupKey.equals(account.groupKey))
            return;

        // Add the new message to be the last message emanating from
        // the given group.  Then rebuild the lists of date header type to group list associations.
        mGroupToLastNewMessageMap.put(message.groupKey, message);
        mDateHeaderTypeToGroupListMap.clear();
        long nowTimestamp = new Date().getTime();
        for (String key : mGroupToLastNewMessageMap.keySet()) {
            // Determine which date header type the current group should be associated with.
            long groupTimestamp = mGroupToLastNewMessageMap.get(key).createTime;
            for (ListItem.DateHeaderType dht : ListItem.DateHeaderType.values()) {
                // Determine if the current group fits the constraints of the current date header
                // type.  The declaration of DateHeaderType is ordered so that this algorithm will
                // work.
                if (dht == old || nowTimestamp - groupTimestamp <= dht.limit) {
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
