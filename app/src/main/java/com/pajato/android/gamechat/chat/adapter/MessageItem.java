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

package com.pajato.android.gamechat.chat.adapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.database.DatabaseManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.ChatListManager.UNREAD_LIST_FORMAT;

/**
 * Provide a POJO to encapsulate a message item to be added to a recycler view.
 *
 * @author Paul Michael Reilly
 */
public class MessageItem {

    // Private class constants.

    // Public instance variables.

    /** The group key */
    public String groupKey;

    /** The room key. */
    public String roomKey;

    /** The owner (poster) name. */
    public String name;

    /** The message text. */
    String text;

    // Public constructors.

    /** Build an instance for the given group. */
    public MessageItem(final String groupKey, final String roomKey, final Message message) {
        // Update the group and room keys, the message text and set the count to 0 to flag that it
        // is not relevant for a message item.  Set the name field to the poster's display name and
        // the creation date.
        this.groupKey = groupKey;
        this.roomKey = roomKey;
        text = message.text;
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        String timestamp = dateFormat.format(new Date(message.createTime));
        name = String.format(Locale.getDefault(), "%s  %s", message.name, timestamp);

        // Flag the message as having been seen by this room member by removing the key from the
        // unread list.
        String accountId = AccountManager.instance.getCurrentAccountId();
        List<String> unreadList = message.unreadList;
        if (unreadList != null && unreadList.contains(accountId)) {
            // The message is still marked new.  Change it to "seen" by removing the key and
            // persisting the message.
            unreadList.remove(accountId);
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            String key = message.messageKey;
            String path = String.format(Locale.US, UNREAD_LIST_FORMAT, groupKey, roomKey, key);
            Map<String, Object> unreadMap = new HashMap<>();
            unreadMap.put("unreadList", unreadList);
            DatabaseManager.instance.updateChildren(database, path, unreadMap);
        }
    }

}
