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

package com.pajato.android.gamechat.database.handler;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.MessageChangeEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.event.MessageChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.MOVED;
import static com.pajato.android.gamechat.event.MessageChangeEvent.NEW;
import static com.pajato.android.gamechat.event.MessageChangeEvent.REMOVED;

/**
 * Provide a class to handle changes to the messages node.
 *
 * @author Paul Michael Reilly
 */
public class MessageListChangeHandler extends DatabaseEventHandler implements ChildEventListener {

    // Private instance constants.

    /** The logcat format string. */
    private static final String LOG_FORMAT = "%s: {%s, %s}.";

    /** The logcat tag. */
    private static final String TAG = MessageListChangeHandler.class.getSimpleName();

    // Private instance variables.

    /** The group and room data. */
    private String mGroupKey;

    /** The room key. */
    private String mRoomKey;

    // Public constructors.

    /** Build a handler with the given name and path. */
    public MessageListChangeHandler(final String name, final String groupKey, final String roomKey) {
        super(name, MessageManager.instance.getMessagesPath(groupKey, roomKey));
        mGroupKey = groupKey;
        mRoomKey = roomKey;
    }

    /** Deal with a new message. */
    @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        // Log the event and determine if the data snapshot exists, aborting if it does not.
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildAdded", dataSnapshot, s));
        process(dataSnapshot, NEW);
    }

    /** Deal with a change in an existing message. */
    @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildChanged", dataSnapshot, s));
        process(dataSnapshot, CHANGED);
    }

    @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildRemoved", dataSnapshot, null));
        process(dataSnapshot, REMOVED);
    }

    @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, String.format(Locale.US, LOG_FORMAT, "onChildMoved", dataSnapshot, s));
        process(dataSnapshot, MOVED);
    }

    /** ... */
    @Override public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }

    // Private instance methods.

    /** Return a map of messages for the room in the given message. */
    private Map<String, Message> getMessageMap() {
        // Ensure that the room map exists for the group in the master map, creating it if need be.
        Map<String, Message> messageMap;
        Map<String, Map<String, Message>> roomMap;
        if (!MessageManager.instance.messageMap.containsKey(mGroupKey)) {
            // This would be the first entry for the group.  Create an empty room map and associate
            // it with the group key.
            roomMap = new HashMap<>();
            MessageManager.instance.messageMap.put(mGroupKey, roomMap);
        } else {
            // This would be an additional profile for the group.  Determine if it is the first for
            // the room.
            roomMap = MessageManager.instance.messageMap.get(mGroupKey);
            if (roomMap == null) {
                // It is the first profile for the room.  Create a map for the room and associate it
                // with the group.
                roomMap = new HashMap<>();
                MessageManager.instance.messageMap.put(mGroupKey, roomMap);
            }
        }

        // The room map exists.  Determine if the message map needs to be created.
        messageMap = roomMap.get(mRoomKey);
        if (messageMap == null) {
            // The message map needs to be created.  Do it now and associate it with the room.
            messageMap = new HashMap<>();
            roomMap.put(mRoomKey, messageMap);
        }

        return messageMap;
    }

    /** Process the change by updating the database list and notifying the app. */
    private void process(final DataSnapshot snapshot, final int type) {
        // Abort if the data snapshot does not exist.
        if (!snapshot.exists()) return;

        // A snapshot exists.  Extract the message and determine if it has been handled already.  If
        // not, then case on the change type and notify the app.
        Message message = snapshot.getValue(Message.class);
        message.groupKey = mGroupKey;
        message.roomKey = mRoomKey;
        Map<String, Message> messageMap = getMessageMap();
        switch (type) {
            case NEW:
            case CHANGED:
                // Add the profile to the list manager (or replace it if it already exists.)
                messageMap.put(message.key, message);
                break;
            case REMOVED:
                // Update the database list experience profile map by removing the entry (key).
                messageMap.remove(message.key);
                break;
            case MOVED:
            default:
                // Not sure what a moved change means or what to do about it, so do nothing for now.
                break;
        }
        AppEventManager.instance.post(new MessageChangeEvent(message, type));
        AppEventManager.instance.post(new ChatListChangeEvent());
    }
}
