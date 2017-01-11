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
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.adapter.DateHeaderItem;
import com.pajato.android.gamechat.chat.adapter.GroupItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
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

import static com.pajato.android.gamechat.chat.adapter.DateHeaderItem.DateHeaderType.old;

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
    private Map<DateHeaderItem.DateHeaderType, List<String>> mDateHeaderTypeToGroupListMap =
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
        DBUtils.instance.updateChildren(profilePath, group.toMap());
        setWatcher(group.key);
    }

    /** Return a room push key to use with a subsequent room object persistence. */
    public String getGroupKey() {
        return FirebaseDatabase.getInstance().getReference().child(GROUPS_PATH).push().getKey();
    }

    /** Return a list of group push keys associated with a given date header type. */
    public List<String> getGroupList(final DateHeaderItem.DateHeaderType type) {
        return mDateHeaderTypeToGroupListMap.get(type);
    }

    /** Get the data as a set of list items for all groups. */
    public List<ChatListItem> getGroupListData() {
        // Generate a list of items to render in the chat group list by extracting the items based
        // on the date header type ordering.
        List<ChatListItem> result = new ArrayList<>();
        for (DateHeaderItem.DateHeaderType dht : DateHeaderItem.DateHeaderType.values()) {
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

    /** Get a map of messages by room in a given group. */
    public Map<String, Map<String, Message>> getGroupMessages(final String groupKey) {
        return MessageManager.instance.messageMap.get(groupKey);
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
        setWatcher(groupKey);
        return null;
    }

    /** Return the database path to the given group's profile. */
    public String getGroupProfilePath(final String groupKey) {
        return String.format(Locale.US, GROUP_PROFILE_PATH, groupKey);
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

    /** Handle a group profile change by updating the map. */
    @Subscribe public void onGroupProfileChange(@NonNull final ProfileGroupChangeEvent event) {
        // Ensure that the group profile is cacheable.  Abort if not, otherwise cache the group
        // object and access all the member accounts in the group in order to communicate with them.
        String groupKey = event.key;
        if (groupKey == null || event.group == null) return;
        groupMap.put(event.key, event.group);
        for (String key : event.group.memberList) MemberManager.instance.setWatcher(groupKey, key);
    }

    /** Return a list of joined group push keys based on the given item. */
    public List<String> getGroups(final ChatListItem item) {
        List<String> result = new ArrayList<>();
        Account account = AccountManager.instance.getCurrentAccount();
        if (item != null && item.groupKey != null) result.add(item.groupKey);
        if (result.isEmpty() && account != null) result.addAll(account.joinList);
        return result;
    }

    /** Handle a message change event by adding the message into the correct room list.  */
    @Subscribe public void onMessageListChange(@NonNull final MessageChangeEvent event) {
        // Update the date headers for this message and post an event to trigger an adapter refresh.
        updateGroupHeaders(event.message);
        AppEventManager.instance.post(new ChatListChangeEvent());
    }

    /** Update the given group profile on the database. */
    public void updateGroupProfile(final Group group) {
        String path = String.format(Locale.US, GROUP_PROFILE_PATH, group.key);
        group.modTime = new Date().getTime();
        DBUtils.instance.updateChildren(path, group.toMap());
    }

    // Private instance methods.

    /** Setup a database listener for the group profile. */
    private void setWatcher(final String groupKey) {
        // Determine if the group has a profile change watcher.  If so, abort, if not, then set one.
        String path = GroupManager.instance.getGroupProfilePath(groupKey);
        String name = DBUtils.instance.getHandlerName(GROUP_PROFILE_CHANGE_HANDLER, groupKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ProfileGroupChangeHandler(name, path, groupKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Update the headers used to bracket the messages in the main list. */
    private void updateGroupHeaders(final Message message) {
        // Add the new message to be the last message emanating from
        // the given group.  Then rebuild the lists of date header type to group list associations.
        mGroupToLastNewMessageMap.put(message.groupKey, message);
        mDateHeaderTypeToGroupListMap.clear();
        long nowTimestamp = new Date().getTime();
        for (String key : mGroupToLastNewMessageMap.keySet()) {
            // Determine which date header type the current group should be associated with.
            long groupTimestamp = mGroupToLastNewMessageMap.get(key).createTime;
            for (DateHeaderItem.DateHeaderType dht : DateHeaderItem.DateHeaderType.values()) {
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
