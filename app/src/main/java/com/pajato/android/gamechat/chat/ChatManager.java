/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.chat;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.chat.adapter.GroupListItem;
import com.pajato.android.gamechat.chat.adapter.RoomsListItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.DatabaseEventHandler;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.event.JoinedRoomListChangeEvent;
import com.pajato.android.gamechat.event.MessageChangeEvent;
import com.pajato.android.gamechat.event.MessageListChangeEvent;
import com.pajato.android.gamechat.event.ProfileChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides the interface to the database/back end, primarily Firebase.
 *
 * @author Paul Michael Reilly
 */
public enum ChatManager {
    instance;

    // Private class constants.

    /** The format specifying the path to the rooms profile on Firebase. */
    private static final String GROUP_PROFILE_PATH = "/groups/%s/profile/";

    /** The format specifying the path to the rooms profile on Firebase. */
    private static final String ROOMS_PROFILE_PATH = "/groups/%s/rooms/%s/profile/";

    // Private instance variables.

    /** The collection of profiles for the joined groups, keyed by the gruop push key. */
    private Map<String, Group> mGroupProfileMap = new HashMap<>();

    /** The collection of messages in the rooms in a group, keyed by the group push key. */
    private Map<String, Map<String, List<Message>>> mGroupMessageMap = new HashMap<>();

    /** The collection of profiles for the joined rooms, keyed by the room push key. */
    private Map<String, Room> mRoomProfileMap = new HashMap<>();

    // Public instance methods.

    /** Get the data as a set of list items. */
    public List<RoomsListItem> getData() {
        // Walk through all the messages and parcel each into a map keyed by a data header value.
        List<RoomsListItem> result = new ArrayList<>();

        for (String groupKey : mGroupMessageMap.keySet()) {
            result.add(new RoomsListItem(new GroupListItem(groupKey)));
        }

        // Build the result list by walking through each map entries using the date header ordinal
        // value to sequence the entries.

        return result;
    }

    /** Get the profile for a given group. */
    public Group getGroupProfile(final String groupKey) {
        return mGroupProfileMap.get(groupKey);
    }

    /** Get a map of messages by room in a given group. */
    public Map<String, List<Message>> getGroupMessages(final String groupKey) {
        return mGroupMessageMap.get(groupKey);
    }

    /** Get the profile for a given room. */
    public Room getRoomProfile(final String roomKey) {
        return mRoomProfileMap.get(roomKey);
    }

    /** Initialize the component. */
    public void init() {
        // Set up the chat database listeners.
        EventBusManager.instance.register(this);
    }

    /** Handle a authentication event. */
    @Subscribe public void onAuthStateChanged(@NonNull final AccountStateChangeEvent event) {
        // Register an active rooms change handler on the account, if there is an account..
        if (event.account != null) {
            // There is an active account.  Register it.
            String path = String.format("/accounts/%s/joinedRoomList", event.account.accountId);
            String name = "joinedRoomListChangeHandler";
            DatabaseEventHandler handler = new JoinedRoomListChangeHandler(name, path);
            DatabaseManager.instance.registerHandler(handler);
        }
    }

    /** Handle a room profile change by updating the map. */
    @Subscribe public void onGroupProfileChange(@NonNull final ProfileChangeEvent<Group> event) {
        mGroupProfileMap.put(event.key, event.t);
    }

    /** Handle changes to the list of joined rooms by capturing all group and room profiles. */
    @Subscribe public void onJoinedRoomsChange(@NonNull final JoinedRoomListChangeEvent event) {
        DatabaseEventHandler handler;
        for (String entry : event.joinedRoomList) {
            // Kick off a value event listener for the group profile.  Tag each listener with the
            // room key to ensure only one listener per group is ever active at any one time.
            String[] split = entry.split(" ");
            String groupKey = split[0];
            String roomKey = split[1];
            String path = String.format(Locale.US, GROUP_PROFILE_PATH, groupKey);
            String name = "profileChangeHandler" + groupKey;
            handler = new ProfileChangeHandler<Group>(name, path, groupKey, Group.class);
            DatabaseManager.instance.registerHandler(handler);

            // Kick off a value event listener for the room profile.  Tag each listener with the
            // room key to ensure only one listener per room is ever active at any one time.
            path = String.format(Locale.US, ROOMS_PROFILE_PATH, groupKey, roomKey);
            name = "profileChangeHandler" + roomKey;
            handler = new ProfileChangeHandler<Room>(name, path, roomKey, Room.class);
            DatabaseManager.instance.registerHandler(handler);
        }
    }

    /** Handle a message change event. */
    @Subscribe public void onMessageChange(@NonNull final MessageChangeEvent event) {
        // Collect the message found in the event payload and trigger an adapter update.
        Map<String, List<Message>> roomMap = mGroupMessageMap.get(event.groupKey);
        if (roomMap == null) {
            // Initialize the map of room messages for this group.
            roomMap = new HashMap<>();
            roomMap.put(event.roomKey, new ArrayList<Message>());
            mGroupMessageMap.put(event.groupKey, roomMap);
        }
        List<Message> messageList = roomMap.get(event.roomKey);
        messageList.add(event.message);
        EventBus.getDefault().post(new MessageListChangeEvent());
    }

    /** Handle a room profile change by updating the map. */
    @Subscribe public void onRoomProfileChange(@NonNull final ProfileChangeEvent<Room> event) {
        mRoomProfileMap.put(event.key, event.t);
    }

    /** Setup a Firebase child event listener for the messages in the given joined room. */
    public void setMessageWatcher(final String groupKey, final String roomKey) {
        // There is an active account.  Register it.
        String name = "messagesChangeHandler";
        DatabaseEventHandler handler = new MessagesChangeHandler(name, groupKey, roomKey);
        DatabaseManager.instance.registerHandler(handler);
    }

    // Private inner classes.

    /** Provide a class to handle structural changes to a User's set of joined rooms. */
    private class JoinedRoomListChangeHandler extends DatabaseEventHandler
            implements ValueEventListener {

        // Private instance constants.

        /** The logcat TAG. */
        private final String TAG = this.getClass().getSimpleName();

        // Public constructors.

        /** Build a handler with the given name and path. */
        JoinedRoomListChangeHandler(final String name, final String path) {
            super(name, path);
        }

        /** Get the current set of active rooms using a list of room identifiers. */
        @Override public void onDataChange(final DataSnapshot dataSnapshot) {
            // Determine if any active rooms exist.
            List<String> list = new ArrayList<>();
            GenericTypeIndicator<List<String>> t;
            if (dataSnapshot.exists()) {
                t = new GenericTypeIndicator<List<String>>() {};
                list.addAll(dataSnapshot.getValue(t));
            }
            EventBus.getDefault().post(new JoinedRoomListChangeEvent(list));
        }

        /** ... */
        @Override public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    }

    /** Provide a class to handle structural changes to a User's set of active rooms. */
    private class MessagesChangeHandler extends DatabaseEventHandler
            implements ChildEventListener {
        private static final String MESSSAGES_FORMAT = "/groups/%s/rooms/%s/messages/";

        // Private instance constants.

        /** The logcat TAG. */
        private final String TAG = this.getClass().getSimpleName();

        // Private instance variables.

        /** The group and room data. */
        private String mGroupKey;

        /** The room key. */
        private String mRoomKey;

        // Public constructors.

        /** Build a handler with the given name and path. */
        MessagesChangeHandler(final String name, final String groupKey, final String roomKey) {
            super(name, String.format(Locale.US, MESSSAGES_FORMAT, groupKey,
                    roomKey));
            mGroupKey = groupKey;
            mRoomKey = roomKey;
        }

        @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String format = "A message has been added: {%s, %s}.";
            Log.d(TAG, String.format(Locale.getDefault(), format, dataSnapshot, s));

            // TODO: get all the messages, for now.  Later on this will have to be paginated.
            // Post the message to be collected and displayed.
            if (dataSnapshot.exists()) {
                Message message = dataSnapshot.getValue(Message.class);
                EventBus.getDefault().post(new MessageChangeEvent(mGroupKey, mRoomKey, message));
            } else {
                Log.e(TAG, "The snapshot does not contain a message!");
            }
        }

        @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "A message child has changed.");
        }

        @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "A message child has been removed.");
        }

        @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "A message child has moved.");
        }

        /** ... */
        @Override public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    }

    /** Provide a class to handle changes to a generic profile. */
    private class ProfileChangeHandler<T> extends DatabaseEventHandler
            implements ValueEventListener {

        // Private instance constants.

        /** The logcat TAG. */
        private final String TAG = this.getClass().getSimpleName();

        // Private instance variables.

        /** The discriminant used to build the object value. */
        private Class<T> mType;

        // Public constructors.

        /** Build a handler with the given name, path and key. */
        ProfileChangeHandler(final String name, final String path, final String key,
                             final Class<T> type) {
            super(name, path, key);
            mType = type;
        }

        /** Get the current generic profile. */
        @Override public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
            // Ensure that some data exists.
            if (dataSnapshot.exists()) {
                // There is data.  Publish the room profile to the app.
                T t = dataSnapshot.getValue(mType);
                EventBus.getDefault().post(new ProfileChangeEvent<>(key, t));
            } else {
                Log.e(TAG, "Invalid room key.  No value returned.");
            }
        }

        /** ... */
        @Override public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    }

}
