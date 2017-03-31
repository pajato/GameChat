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

import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.ListItem.DateHeaderType;
import com.pajato.android.gamechat.common.adapter.ListItem.ItemType;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.MessageListChangeHandler;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.model.Message.SYSTEM;
import static com.pajato.android.gamechat.common.adapter.ListItem.DateHeaderType.old;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.date;

/**
 * Provide a class to manage the app interactions with the database for lists of chat messages.
 * Briefly, a message is received from the database.  It might be a new message (just posted by a
 * User) or a startup message (one of a batch occurring in response to setting a listener on a room)
 * or a paged message (in response to a search or scrolling event initiated by the User). In any
 * case the message is added (as part of a background task) to a sorted list (double buffered) and
 * the background processing of the message begins:
 *
 * 1) if a background process is already running on behalf of another message, this new message is
 * queued and will be batch processed along with any others as soon as the current background
 * process is complete.
 *
 * 2) if there is no background process running, a new one is started.  First, the message is added
 * to the sorted list. Then a data structure allowing for the easy association of date headers (now,
 * recent, today, yesterday, etc.) is computed.  This data structure essentially associates a date
 * header with an index into the list of messages and the number of messages in that date header
 * category.  After this background task is completed the data structure is copied (double
 * buffering) and switched onto the main thread as part of a message change event posting.  That
 * posted event will be processed to display lists of messages per the date header type.
 *
 * @author Paul Michael Reilly
 */

public enum MessageManager {
    instance;

    // Public class constants.

    public static final String MESSAGES_PATH = RoomManager.ROOMS_PATH + "%s/messages/";
    public static final String MESSAGE_PATH = MESSAGES_PATH + "%s/";

    // Private class constants.

    /** The message list change handler base name. */
    private static final String MESSAGE_LIST_CHANGE_HANDLER = "messageListChangeHandler";

    // Public instance variables.

    /** The map associating group and room push keys with a map of messages. */
    public Map<String, Map<String, Map<String, Message>>> messageMap = new HashMap<>();

    // Private instance variables.

    // Public instance methods.

    /** Persist a standard message (one sent from a standard user) to the database. */
    public void createMessage(final String text, final int type, @NonNull final Account account,
                              final Room room) {
        // Ensure database consistency.  Abort quietly (for now) if any path parts are invalid.
        // If the parts are valid, get a push key.
        // TODO: say something about an invalid path piece.
        if (room.groupKey == null || room.key == null)
            return;
        String path = String.format(Locale.US, MESSAGES_PATH, room.groupKey, room.key);
        String key = FirebaseDatabase.getInstance().getReference().child(path).push().getKey();

        // The room is valid.  Create the message.
        String systemName = DBUtils.instance.getResource(DBUtils.SYSTEM_NAME_KEY);
        String name = type == SYSTEM ? systemName : account.getDisplayName();
        String systemUrl = "android.resource://com.pajato.android.gamechat/drawable/ic_launcher";
        String url = type == SYSTEM ? systemUrl : account.url;
        long tStamp = new Date().getTime();
        List<String> members = new ArrayList<>();
        members.addAll(room.getMemberIdList());
        Message message = new Message(key, account.id, name, url, tStamp, text, type, members);

        // Persist (i.e. post) the message.
        path = String.format(Locale.US, MESSAGE_PATH, room.groupKey, room.key, key);
        DBUtils.updateChildren(path, message.toMap());
    }

    /** Get a map of messages keyed by room push key in a given group. */
    public Map<String, Map<String, Message>> getGroupMessages(final String groupKey) {
        return messageMap.get(groupKey);
    }

    /** Return a possibly empty list of messages for a given group and room. */
    @SuppressWarnings("unused")
    public List<Message> getMessageList(final String groupKey, final String roomKey) {
        // Ensure there are some messages to be had in the group.  Return the empty list if none
        // are found, otherwise return all messages in that room.
        List<Message> result = new ArrayList<>();
        Map<String, Map<String, Message>> roomMap = getGroupMessages(groupKey);
        if (roomMap == null) return result;
        result.addAll(roomMap.get(roomKey).values());
        return result;
    }

    /** Return a list of messages, an empty list if there are none to be had, for a given item. */
    public List<ListItem> getListItemData(@NonNull final Dispatcher dispatcher) {
        // Generate a map of date header types to a list of messages, i.e. a chronological ordering
        // of the messages.
        List<ListItem> result = new ArrayList<>();
        String groupKey = dispatcher.groupKey;
        String roomKey = getRoomKey(dispatcher);
        if (roomKey == null)
            return result;
        return getItems(getMessageMap(getGroupMessages(groupKey).get(roomKey)));
    }

    /** Return the path to the messages for the given group and room keys. */
    public String getMessagesPath(final String groupKey, final String roomKey) {
        return String.format(Locale.US, MESSAGES_PATH, groupKey, roomKey);
    }

    /** Handle a account change event by setting up or clearing variables. */
    @Subscribe public void onAuthenticationChange(@NonNull final AuthenticationChangeEvent event) {
        // Determine if a User has been authenticated.  If so, do nothing, otherwise clear the
        // message list for the logged out User.
        if (event.account != null) return;
        messageMap.clear();
    }

    /** Remove the listener for messages in the specified room */
    public void removeWatcher(final String roomKey) {
        String name = DBUtils.getHandlerName(MESSAGE_LIST_CHANGE_HANDLER, roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) {
            DatabaseRegistrar.instance.unregisterHandler(name);
        }
    }

    /** Setup a Firebase child event listener for the messages in the given joined room. */
    public void setWatcher(final String groupKey, final String roomKey) {
        // There is an active account.  Register it.
        String name = DBUtils.getHandlerName(MESSAGE_LIST_CHANGE_HANDLER, roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name))
            return;
        DatabaseEventHandler handler = new MessageListChangeHandler(name, groupKey, roomKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Update a message on the database. */
    public void updateMessage(Message message) {
        String groupKey = message.groupKey;
        String roomKey = message.roomKey;
        String path = String.format(Locale.US, MESSAGE_PATH, groupKey, roomKey, message.key);
        DBUtils.updateChildren(path, message.toMap());
    }

    // Private instance methods.

    /** Return the date header type most closely associated with the given message timestamp. */
    private DateHeaderType getDateHeaderType(final Message message) {
        long now = new Date().getTime();
        for (DateHeaderType type : DateHeaderType.values()) {
            // Determine if this is the right dht value.
            if (now - message.createTime <= type.limit) {
                // This is the correct dht value to use. Done.
                return type;
            }
        }
        return old;
    }

    /** Return a list of ordered chat items from a map of chronologically ordered messages. */
    private List<ListItem> getItems(final Map<DateHeaderType, List<Message>> messageMap) {
        // Build the list of display items, in reverse order (oldest to newest).
        List<ListItem> result = new ArrayList<>();
        DateHeaderType[] types = DateHeaderType.values();
        int size = types.length;
        for (int index = size - 1; index >= 0; index--) {
            // Add the header item followed by all the room messages.
            DateHeaderType dht = types[index];
            List<Message> list = messageMap.get(dht);
            if (list == null)
                continue;
            result.add(new ListItem(date, dht.resId));
            Collections.sort(list, new MessageComparator());
            for (Message message : list) {
                String groupKey = message.groupKey;
                String roomKey = message.roomKey;
                DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
                String tStamp = dateFormat.format(new Date(message.createTime));
                String name = String.format(Locale.getDefault(), "%s  %s", message.name, tStamp);
                result.add(new ListItem(ItemType.message, groupKey, roomKey, name, message.text,
                        message.url, message.key));
            }
        }
        return result;
    }

    /** Return null or a valid room key for the given configuration. */
    private String getRoomKey(@NonNull final Dispatcher dispatcher) {
        if (dispatcher.groupKey == null)
            return null;
        if (dispatcher.groupKey.equals(AccountManager.instance.getMeGroupKey()))
            return AccountManager.instance.getMeRoomKey();
        return dispatcher.roomKey;
    }

    /** Sort lists of messages after they've been sorted into date categories. */
    private class MessageComparator implements Comparator<Message> {
        @Override public int compare(Message m1, Message m2) {
            Date d1 = new Date(m1.createTime);
            Date d2 = new Date(m2.createTime);
            return d1.compareTo(d2);
        }
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
}
