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

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.game.Experience;
import com.pajato.android.gamechat.game.model.ExpProfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.R.string.me;
import static com.pajato.android.gamechat.chat.model.Message.SYSTEM;
import static com.pajato.android.gamechat.chat.model.Room.ME;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public enum DatabaseManager {
    instance;

    // Private class constants.

    // Database paths, often used as format strings.
    private static final String ACCOUNT_PATH = "/accounts/%s/";
    private static final String GROUPS_PATH = "/groups/";
    private static final String GROUP_PROFILE_PATH = GROUPS_PATH + "%s/profile/";
    private static final String JOINED_ROOM_LIST_PATH = ACCOUNT_PATH + "joinedRoomList";
    private static final String ROOMS_PATH = GROUPS_PATH + "%s/rooms/";
    private static final String ROOM_PROFILE_PATH = ROOMS_PATH + "%s/profile/";
    private static final String EXP_PROFILE_LIST_PATH = ROOMS_PATH + "%s/profile/expProfileList";
    private static final String EXPERIENCES_PATH = ROOMS_PATH + "%s/experiences/";
    private static final String EXPERIENCE_PATH = EXPERIENCES_PATH + "%s/";
    private static final String MESSAGES_PATH = ROOMS_PATH + "%s/messages/";
    private static final String MESSAGE_PATH = MESSAGES_PATH + "%s/";
    private static final String UNREAD_LIST_PATH = MESSAGES_PATH + "%s/unreadList";

    // Lookup keys.
    private static final String ANONYMOUS_NAME_KEY = "anonymousNameKey";
    private static final String DEFAULT_ROOM_NAME_KEY = "defaultRoomNameKey";
    private static final String ME_GROUP_KEY = "meGroupKey";
    private static final String ME_NAME_KEY = "meNameKey";
    private static final String ME_ROOM_KEY = "meRoomKey";
    private static final String SYSTEM_NAME_KEY = "systemNameKey";

    // Public instance variables.

    /** The database reference object. */
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    /** The map providing localized resources, setup during initialization. */
    private Map<String, String> mResourceMap = new HashMap<>();

    // Public instance methods.

    /** Return the default room name and group key from the given room as a joined list entry. */
    public void appendDefaultJoinedRoomEntry(final Account account, final Group group) {
        String name = mResourceMap.get(DEFAULT_ROOM_NAME_KEY);
        String roomKey = group.roomMap.get(name);
        String entry = AccountManager.instance.getJoinedRoomEntry(group.key, roomKey);
        if (roomKey != null) account.joinedRoomList.add(entry);
    }

    /** Create and persist an account to the database. */
    public void createAccount(@NonNull Account account) {
        // Set up the push keys for the account, default "me" group and room.
        String groupKey = mDatabase.child(GROUPS_PATH).push().getKey();
        String path = String.format(Locale.US, ROOMS_PATH, groupKey);
        String roomKey = mDatabase.child(path).push().getKey();

        // Set up and persist the account for the given user.
        long tstamp = account.createTime;
        account.groupIdList.add(groupKey);
        account.joinedRoomList.add(AccountManager.instance.getJoinedRoomEntry(groupKey, roomKey));
        updateChildren(String.format(Locale.US, ACCOUNT_PATH, account.id), account.toMap());

        // Update the group profile on the database.
        List<String> memberList = new ArrayList<>();
        memberList.add(account.id);
        String name = mResourceMap.get(ME_GROUP_KEY);
        Map<String, String> roomMap = new HashMap<>();
        roomMap.put(name, roomKey);
        Group group = new Group(groupKey, account.id, name, tstamp, 0, memberList, roomMap);
        updateChildren(String.format(Locale.US, GROUP_PROFILE_PATH, groupKey), group.toMap());

        // Update the "me" room profile on the database.
        name = mResourceMap.get(ME_ROOM_KEY);
        Room room = new Room(roomKey, account.id, name, groupKey, tstamp, 0, ME, memberList);
        String roomProfilePath = String.format(Locale.US, ROOM_PROFILE_PATH, groupKey, roomKey);
        updateChildren(roomProfilePath, room.toMap());

        // Update the "me" room default message on the database.
        String text = "Welcome to your own private group and room.  Enjoy!";
        createMessage(text, SYSTEM, account, room);
    }

    /** Persist the given experience to the database. */
    public void createExperience(final Experience experience) {
        // Ensure that the requisite keys exist.  Abort if either key does not exist.
        String groupKey = experience.getGroupKey();
        String roomKey = experience.getRoomKey();
        if (groupKey == null || roomKey == null) return;

        // Get the name and type for the given experience.  Abort if either does not exist.
        String name = experience.getName();
        int type = experience.getType();
        if (name == null || type == -1) return;

        // Persist the experience.
        String path = String.format(Locale.US, EXPERIENCES_PATH, groupKey, roomKey);
        DatabaseReference ref = mDatabase.child(path).push();
        experience.setExperienceKey(ref.getKey());
        ref.setValue(experience.toMap());

        // Persist the experience profile so that the room's experience profile list watcher will
        // append it.  Finally set the experience watcher as, presumbably, the User is creating the
        // experience to enjoy it asap.
        path = String.format(Locale.US, EXP_PROFILE_LIST_PATH, groupKey, roomKey);
        ref = mDatabase.child(path).push();
        String key = ref.getKey();
        String expKey = experience.getExperienceKey();
        ExpProfile profile = new ExpProfile(key, name, type, groupKey, roomKey, expKey);
        ref.setValue(profile.toMap());
        DatabaseListManager.instance.setExperienceWatcher(profile);
    }

    /** Persist a given group object using the given key. */
    public void createGroupProfile(final Group group) {
        // Ensure that a valid group key exists.  Abort quietly (for now) if not.
        // TODO: do something about a null group key.
        if (group.key == null) return;
        String profilePath = String.format(Locale.US, GROUP_PROFILE_PATH, group.key);
        group.createTime = new Date().getTime();
        updateChildren(profilePath, group.toMap());
    }

    /** Persist a standard message (one sent from a standard user) to the database. */
    public void createMessage(final String text, final int type, @NonNull final Account account,
                              final Room room) {
        // Ensure database consistency.  Abort quietly (for now) if any path parts are invalid.
        // If the parts are valid, get a push key.
        // TODO: say saomething about an onvalid path piece.
        if (room.groupKey == null || room.key == null) return;
        String path = String.format(Locale.US, MESSAGES_PATH, room.groupKey, room.key);
        String key = mDatabase.child(path).push().getKey();

        // The room is valid.  Create the message.
        String systemName = mResourceMap.get(SYSTEM_NAME_KEY);
        String anonymousName = mResourceMap.get(ANONYMOUS_NAME_KEY);
        String name = type == SYSTEM ? systemName : account.getDisplayName(anonymousName);
        String systemUrl = "android.resource://com.pajato.android.gamechat/drawable/ic_launcher";
        String url = type == SYSTEM ? systemUrl : account.url;
        long tstamp = new Date().getTime();
        List<String> members = room.memberIdList;
        Message message = new Message(key, account.id, name, url, tstamp, 0, text, type, members);

        // Persist the message.
        path = String.format(Locale.US, MESSAGE_PATH, room.groupKey, room.key, key);
        updateChildren(path, message.toMap());
    }

    /** Return a room push key resulting from persisting the given room on the database. */
    public void createRoomProfile(final Room room) {
        // Ensure that a valid group key exists.  Abort quietly (for now) if not.
        // TODO: do something about a null group key.
        if (room.groupKey == null || room.key == null) return;
        String profilePath = String.format(Locale.US, ROOM_PROFILE_PATH, room.groupKey, room.key);
        room.createTime = new Date().getTime();
        updateChildren(profilePath, room.toMap());
    }

    /** Return the database path to an experience for a given experience profile. */
    public String getExperiencePath(final ExpProfile profile) {
        String groupKey = profile.groupKey;
        String roomKey = profile.roomKey;
        return String.format(Locale.US, EXPERIENCE_PATH, groupKey, roomKey, profile.key);
    }

    /** Return the database path to a experience profile for a given room and profile key. */
    public String getExpProfilesPath(final String groupKey, final String roomKey) {
        return String.format(Locale.US, EXP_PROFILE_LIST_PATH, groupKey, roomKey);
    }

    /** Return a room push key to use with a subsequent room object persistence. */
    public String getGroupKey() {
        return mDatabase.child(GROUPS_PATH).push().getKey();
    }

    /** Return the database path to the given group's profile. */
    public String getGroupProfilePath(final String groupKey) {
        return String.format(Locale.US, GROUP_PROFILE_PATH, groupKey);
    }

    /** Return the database path to the joined list in a given account. */
    public String getJoinedRoomListPath(final Account account) {
        return String.format(Locale.US, JOINED_ROOM_LIST_PATH, account.id);
    }

    /** Return the path to the messages for the given group and room keys. */
    public String getMessagesPath(final String groupKey, final String roomKey) {
        return String.format(Locale.US, MESSAGES_PATH, groupKey, roomKey);
    }

    /** Return a room push key to use with a subsequent room object persistence. */
    public String getRoomKey(final String groupKey) {
        String roomsPath = String.format(Locale.US, ROOMS_PATH, groupKey);
        return mDatabase.child(roomsPath).push().getKey();
    }

    /** Return the database path to the given group's profile. */
    public String getRoomProfilePath(final String groupKey, final String roomKey) {
        return String.format(Locale.US, ROOM_PROFILE_PATH, groupKey, roomKey);
    }

    /** Intialize the database manager by setting up localized resources. */
    public void init(final Context context) {
        mResourceMap.clear();
        mResourceMap.put(ME_GROUP_KEY, context.getString(R.string.DefaultPrivateGroupName));
        mResourceMap.put(ME_ROOM_KEY, context.getString(R.string.DefaultPrivateRoomName));
        mResourceMap.put(SYSTEM_NAME_KEY, context.getString(R.string.app_name));
        mResourceMap.put(ANONYMOUS_NAME_KEY, context.getString(R.string.anonymous));
        mResourceMap.put(ME_NAME_KEY, context.getString(me));
        mResourceMap.put(DEFAULT_ROOM_NAME_KEY, context.getString(R.string.DefaultRoomName));
    }

    /** Update the given account on the database. */
    public void updateAccount(final Account account) {
        String path = String.format(Locale.US, ACCOUNT_PATH, account.id);
        account.modTime = new Date().getTime();
        updateChildren(path, account.toMap());
    }

    /** Store an object on the database using a given path, pushKey, and properties. */
    public void updateChildren(final String path, final Map<String, Object> properties) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(path, properties);
        mDatabase.updateChildren(childUpdates);
    }

    /** Update the given group on the database. */
    public void updateGroup(final Group group, final String groupKey) {
        String path = String.format(Locale.US, GROUP_PROFILE_PATH, groupKey);
        group.modTime = new Date().getTime();
        updateChildren(path, group.toMap());
    }

    /** Persist the given message to reflect a change to the unread list. */
    public void updateUnreadList(final String groupKey, final String roomKey,
                                 final Message message) {
        String key = message.key;
        String path = String.format(Locale.US, UNREAD_LIST_PATH, groupKey, roomKey, key);
        Map<String, Object> unreadMap = new HashMap<>();
        unreadMap.put("unreadList", message.unreadList);
        DatabaseManager.instance.updateChildren(path, unreadMap);
    }

}
