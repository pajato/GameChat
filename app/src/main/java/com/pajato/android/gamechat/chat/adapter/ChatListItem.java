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

import java.util.Locale;

import static android.R.attr.key;

/**
 * Provide a POJO to encapsulate a recycler view list item: either a date label view or a room list
 * view showing the rooms in a group with messages characterized by a preceding date label view.
 *
 * @author Paul Michael Reilly
 */
public class ChatListItem {

    // Type constants.
    static final int CONTACT_HEADER_ITEM_TYPE = 0;
    static final int CONTACT_ITEM_TYPE = 1;
    static final int DATE_ITEM_TYPE = 2;
    public static final int GROUP_ITEM_TYPE= 3;
    static final int MESSAGE_ITEM_TYPE = 4;
    public static final int ROOM_ITEM_TYPE = 5;

    // Public enums

    // Public instance variables.

    /** The chat list item count of new messages in a group or a room. */
    int count;

    /** A description of the item. */
    public String desc;

    /** The item email address, possibly null, used for contact items. */
    public String email;

    /** The group (push) key, possibly null, used for chat list items (groups, rooms, messages) */
    public String groupKey;

    /** The item name, possibly null, used for all items. */
    public String name;

    /** The item name resource identifier. */
    int nameResourceId;

    /** The item phone number, possibly null, used for contact items. */
    String phone;

    /** The room (push) key, possibly null, used for chat list items. */
    public String roomKey;

    /** The item type, always non-null. */
    public int type;

    /** The URL for the item, possibly null, used for icons with contacts and chat list items. */
    public String url;

    /** The list of rooms or groups with messages to show, or the text of a message. */
    public String text;

    // Public constructors.

    /** Build an instance for a given contact header item. */
    public ChatListItem(final ContactHeaderItem item) {
        type = CONTACT_HEADER_ITEM_TYPE;
        nameResourceId = item.getNameResourceId();
        desc = String.format(Locale.US, "Contact header with id: {%d}.", nameResourceId);
    }

    /** Build an instance for a given contact list item. */
    public ChatListItem(final ContactItem item) {
        type = CONTACT_ITEM_TYPE;
        name = item.name;
        email = item.email;
        phone = item.phone;
        url = item.url;
        String format = "Contact item with name {%s}, email: {%s}, phone: {%s} and url {%s}.";
        desc = String.format(Locale.US, format, name, email, phone, url);
    }

    /** Build an instance for a given group list item. */
    public ChatListItem(final GroupItem item) {
        type = GROUP_ITEM_TYPE;
        groupKey = item.groupKey;
        name = item.name;
        count = item.count;
        text = item.text;
        String format = "Group item with name {%s}, key: {%s}, count: {%s} and text {%s}.";
        desc = String.format(Locale.US, format, name, key, count, text);
    }

    /** Build an instance for a given date header item. */
    public ChatListItem(final DateHeaderItem item) {
        type = DATE_ITEM_TYPE;
        nameResourceId = item.getNameResourceId();
        desc = String.format(Locale.US, "Contact header with id: {%d}.", nameResourceId);
    }

    /** Build an instance for a given room list item. */
    public ChatListItem(final MessageItem item) {
        type = MESSAGE_ITEM_TYPE;
        groupKey = item.groupKey;
        roomKey = item.roomKey;
        name = item.name;
        count = 0;
        text = item.text;
        url = item.url;
        String format = "Message item with name {%s}, key: {%s}, count: {%s} and text {%s}.";
        desc = String.format(Locale.US, format, name, key, count, text);
    }

    /** Build an instance for a given room list item. */
    public ChatListItem(final RoomItem item) {
        type = ROOM_ITEM_TYPE;
        groupKey = item.groupKey;
        roomKey = item.roomKey;
        name = item.name;
        count = item.count;
        text = item.text;
        String format = "Room item with name {%s}, key: {%s}, count: {%s} and text {%s}.";
        desc = String.format(Locale.US, format, name, key, count, text);
    }

}
