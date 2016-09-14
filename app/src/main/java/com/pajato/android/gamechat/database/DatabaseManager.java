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
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.model.Message.SYSTEM;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public enum DatabaseManager {
    instance;

    // Private class constants.

    /** The path to the account node on the database. */
    private static final String ACCOUNT_FORMAT = "/accounts/%s/";

    /** The lookup key for the localized "anonymous" name. */
    private static final String ANONYMOUS_NAME_KEY = "anonymousNameKey";

    /** The path to the groups node on the database. */
    private static final String GROUPS_PATH = "/groups/";

    /** The path format for a group profile. */
    private static final String GROUP_PROFILE_FORMAT = GROUPS_PATH + "%s/profile/";

    /** The path format for detecting changes to the joined room list. */
    private static final String JOINED_ROOM_LIST_FORMAT = ACCOUNT_FORMAT + "joinedRoomList";

    /** The path format for the rooms node within a group. */
    private static final String ROOMS_FORMAT = GROUPS_PATH + "%s/rooms/";

    /** The path format for the profile node within a room. */
    private static final String ROOM_PROFILE_FORMAT = ROOMS_FORMAT + "%s/profile/";

    /** The path format for a message. */
    private static final String MESSAGES_FORMAT = ROOMS_FORMAT + "%s/messages/";

    /** The path format for a message. */
    private static final String MESSAGE_FORMAT = MESSAGES_FORMAT + "%s/";

    /** The default group name (the "me" group) key. */
    private static final String ME_GROUP_KEY = "meGroupKey";

    /** The lookup key for the localized "me" name. */
    private static final String ME_NAME_KEY = "meNameKey";

    /** The default room name (the "me" room) key. */
    private static final String ME_ROOM_KEY = "meRoomKey";

    /** The format used to generate the database path for a particular message. */
    private static final String UNREAD_LIST_FORMAT = MESSAGES_FORMAT + "%s/unreadList";

    /** The default system name (GameChat) key. */
    private static final String SYSTEM_NAME_KEY = "systemNameKey";

    /** The logcat tag. */
    private static final String TAG = DatabaseManager.class.getSimpleName();

    // Public instance variables.

    /** The database reference object. */
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    /** The map providing localized resources. */
    private Map<String, String> mResourceMap = new HashMap<>();

    /** The Firebase value event listener map. */
    private Map<String, DatabaseEventHandler> mHandlerMap = new HashMap<>();

    // Public instance methods.

    /** Create and persist an account to the database. */
    public void createAccount(@NonNull FirebaseUser user, final int accountType) {
        // Set up the push keys for the account, default "me" group and room.
        String groupKey = mDatabase.child(GROUPS_PATH).push().getKey();
        String path = String.format(Locale.US, ROOMS_FORMAT, groupKey);
        String roomKey = mDatabase.child(path).push().getKey();

        // Set up and persist the account for the given user.
        long tstamp = new Date().getTime();
        Account account = new Account();
        account.id = user.getUid();
        account.email = user.getEmail();
        account.displayName = user.getDisplayName();
        account.url = AccountManager.instance.getPhotoUrl(user);
        account.providerId = user.getProviderId();
        account.type = accountType;
        account.createTime = tstamp;
        account.groupIdList.add(groupKey);
        account.joinedRoomList.add(groupKey + " " + roomKey);
        updateChildren(String.format(Locale.US, ACCOUNT_FORMAT, account.id), account.toMap());

        // Update the group profile on the database.
        List<String> memberList = new ArrayList<>();
        memberList.add(account.id);
        String name = mResourceMap.get(ME_GROUP_KEY);
        Group group = new Group(account.id, name, tstamp, 0, memberList);
        updateChildren(String.format(Locale.US, GROUP_PROFILE_FORMAT, groupKey), group.toMap());

        // Update the "me" room profile on the database.
        name = mResourceMap.get(ME_ROOM_KEY);
        Room room = new Room(account.id, name, groupKey, tstamp, 0, "me", memberList);
        String roomProfilePath = String.format(Locale.US, ROOM_PROFILE_FORMAT, groupKey, roomKey);
        updateChildren(roomProfilePath, room.toMap());

        // Update the "me" room default message on the database.
        String text = "Welcome to your own private group and room.  Enjoy!";
        createMessage(text, SYSTEM, account, groupKey, roomKey, room);
    }

    /** Return a group push key resulting from persisting the given group on the database. */
    public String createGroupProfile(final Group group) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String groupKey = database.child(GROUPS_PATH).push().getKey();
        String profilePath = String.format(Locale.US, GROUP_PROFILE_FORMAT, groupKey);
        updateChildren(profilePath, group.toMap());

        return groupKey;
    }

    /** Persist a standard message (one sent from a standard user) to the database. */
    public void createMessage(final String text, final int type, @NonNull final Account account,
                              final String groupKey, final String roomKey, final Room room) {
        String path = String.format(Locale.US, MESSAGES_FORMAT, groupKey, roomKey);
        String key = mDatabase.child(path).push().getKey();
        String id = account.id;
        String name = getName(type, account);
        String url = getPhotoUrl(type, account);
        long tstamp = new Date().getTime();
        List<String> members = room.memberIdList;
        Message message = new Message(id, name, url, key, tstamp, 0, text, type, members);
        path = String.format(Locale.US, MESSAGE_FORMAT, groupKey, roomKey, key);
        updateChildren(path, message.toMap());
    }

    /** Return a room push key resulting from persisting the given room on the database. */
    public String createRoomProfile(final String groupKey, final Room room) {
        String roomsPath = String.format(Locale.US, ROOMS_FORMAT, groupKey);
        String roomKey = mDatabase.child(roomsPath).push().getKey();
        String profilePath = String.format(Locale.US, ROOM_PROFILE_FORMAT, groupKey, roomKey);
        updateChildren(profilePath, room.toMap());

        return roomKey;
    }

    /** Return the database path to the given group's profile. */
    public String getGroupProfilePath(final String groupKey) {
        return String.format(Locale.US, GROUP_PROFILE_FORMAT, groupKey);
    }

    /** Return the database path to the joined list in a given account. */
    public String getJoinedRoomListPath(final Account account) {
        return String.format(Locale.US, JOINED_ROOM_LIST_FORMAT, account.id);
    }

    /** Return the database path to the given group's profile. */
    public String getRoomProfilePath(final String groupKey, final String roomKey) {
        return String.format(Locale.US, ROOM_PROFILE_FORMAT, groupKey, roomKey);
    }

    /** Return a handler registered with the given name, null if one is not found. */
    public DatabaseEventHandler getHandler(final String name) {
        return mHandlerMap.get(name);
    }

    /** Intialize the database manager by setting up localized resources. */
    public void init(final Context context) {
        mResourceMap.clear();
        mResourceMap.put(ME_GROUP_KEY, context.getString(R.string.DefaultPrivateGroupName));
        mResourceMap.put(ME_ROOM_KEY, context.getString(R.string.DefaultPrivateRoomName));
        mResourceMap.put(SYSTEM_NAME_KEY, context.getString(R.string.app_name));
        mResourceMap.put(ANONYMOUS_NAME_KEY, context.getString(R.string.anonymous));
        mResourceMap.put(ME_NAME_KEY, context.getString(R.string.me));
    }

    /** Return TRUE iff the a handler with the given name is registered. */
    public boolean isRegistered(String name) {
        return mHandlerMap.containsKey(name);
    }

    /** Register a given value event listener. */
    public void registerHandler(final DatabaseEventHandler handler) {
        // Determine if there is already a listener registered with this name.
        String name = handler.name;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(handler.path);
        DatabaseEventHandler registeredHandler = mHandlerMap.get(name);
        removeEventListener(database, registeredHandler);

        // Register the new listener both with the handler map and with Firebase.
        mHandlerMap.put(name, handler);
        ValueEventListener valueEventListener = handler instanceof ValueEventListener ?
            (ValueEventListener) handler : null;
        ChildEventListener childEventListener = handler instanceof ChildEventListener ?
            (ChildEventListener) handler : null;
        mHandlerMap.put(name, handler);
        if (valueEventListener != null) {
            database.addValueEventListener(valueEventListener);
        }
        if (childEventListener != null) {
            database.addChildEventListener(childEventListener);
        }
    }

    /** Unregister all listeners. */
    public void unregisterAll() {
        // Walk the set of registered handlers to remove them.  Leave them cached for possible reuse.
        DatabaseReference database;
        DatabaseEventHandler handler;
        for (String name : mHandlerMap.keySet()) {
            handler = mHandlerMap.get(name);
            database = FirebaseDatabase.getInstance().getReference(handler.path);
            removeEventListener(database, handler);
        }
    }

    /** Unregister a named listener. */
    public void unregisterHandler(final String name) {
        // Determine if there is a handler registered by the given name.
        DatabaseEventHandler handler = mHandlerMap.get(name);
        if (handler != null) {
            // There is.  Remove it as a listener but keep it cached for possible reuse.
            DatabaseReference database = FirebaseDatabase.getInstance().getReference(handler.path);
            removeEventListener(database, handler);
        }
    }

    /** Update the given account on the database. */
    public void updateAccount(final Account account) {
        String path = String.format(Locale.US, ACCOUNT_FORMAT, account.id);
        account.modTime = new Date().getTime();
        updateChildren(path, account.toMap());
    }

    /** Store an object on the database using a given path, pushKey, and properties. */
    public void updateChildren(final String path, final Map<String, Object> properties) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(path, properties);
        mDatabase.updateChildren(childUpdates);
    }

    /** Persist the given message to reflect a change to the unread list. */
    public void updateUnreadList(final String groupKey, final String roomKey,
                                 final Message message) {
        String key = message.messageKey;
        String path = String.format(Locale.US, UNREAD_LIST_FORMAT, groupKey, roomKey, key);
        Map<String, Object> unreadMap = new HashMap<>();
        unreadMap.put("unreadList", message.unreadList);
        DatabaseManager.instance.updateChildren(path, unreadMap);
    }

    // Private instance methods.

    /** Return the message display name for a given type and account. */
    private String getName(final int type, final Account account) {
        // If the type is system, return the localized system name.
        if (type == SYSTEM) return mResourceMap.get(SYSTEM_NAME_KEY);

        // Return the localized dislay name.
        String me = mResourceMap.get(ME_NAME_KEY);
        String anonymous = mResourceMap.get(ANONYMOUS_NAME_KEY);
        return account.getDisplayName(account, me, anonymous);
    }

    /** Return the message photo url (possibly null) for a given type and account. */
    private String getPhotoUrl(final int type, final Account account) {
        String systemUrl = "android.resource://com.pajato.android.gamechat/drawable/ic_launcher";
        if (type == SYSTEM) return systemUrl;
        return account.url;
    }

    /** Remove the database event listener, if any is found associated with the given handler. */
    private void removeEventListener(final DatabaseReference database,
                                     final DatabaseEventHandler handler) {
        ValueEventListener valueEventListener;
        ChildEventListener childEventListener;
        if (handler != null) {
            // There is a handler found.  Remove it.
            if (handler instanceof ValueEventListener) {
                valueEventListener = (ValueEventListener) handler;
                database.removeEventListener(valueEventListener);
            } else if (handler instanceof ChildEventListener) {
                childEventListener = (ChildEventListener) handler;
                database.removeEventListener(childEventListener);
            } else {
                // Remove the funky entry, after logging it.
                String format = "Removing an invalid event listener, %s/%s (name/type).";
                String name = handler.name;
                String type = handler.getClass().getSimpleName();
                Log.e(TAG, String.format(Locale.getDefault(), format, name, type));
            }
        }
    }

}
