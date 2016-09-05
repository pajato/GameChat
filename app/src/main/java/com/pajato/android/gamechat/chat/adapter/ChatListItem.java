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

/**
 * Provide a POJO to encapsulate a recycler view list item: either a date label view or a room list
 * view showing the rooms in a group with messages characterized by a preceding date label view.
 *
 * @author Paul Michael Reilly
 */
public class ChatListItem {

    // Public constants.

    static final int DATE_ITEM_TYPE = 0;
    static final int GROUP_ITEM_TYPE= 1;
    static final int MESSAGE_ITEM_TYPE = 2;
    static final int ROOM_ITEM_TYPE = 3;

    // Public enums

    // Public instance variables.

    /** The group (push) key. */
    public String groupKey;

    /** The room (push) key, possibly null. */
    public String roomKey;

    /** The item type. */
    public int type;

    /** The item name, possibly null. */
    public String name;

    /** The item name resource identifier. */
    int nameResourceId;

    /** The chat list item count of new messages in a group or a room. */
    int count;

    /** The list of rooms or groups with messages to show, or the text of a message. */
    public String text;

    // Public constructors.

    /** Build an instance for a given group list item. */
    public ChatListItem(final GroupItem item) {
        type = GROUP_ITEM_TYPE;
        groupKey = item.groupKey;
        name = item.name;
        count = item.count;
        text = item.text;
    }

    /** Build an instance for a given date header item. */
    public ChatListItem(final DateHeaderItem item) {
        type = DATE_ITEM_TYPE;
        nameResourceId = item.getNameResourceId();
    }

    /** Build an instance for a given room list item. */
    public ChatListItem(final MessageItem item) {
        type = MESSAGE_ITEM_TYPE;
        groupKey = item.groupKey;
        roomKey = item.roomKey;
        name = item.name;
        count = 0;
        text = item.text;
    }

    /** Build an instance for a given room list item. */
    public ChatListItem(final RoomItem item) {
        type = ROOM_ITEM_TYPE;
        groupKey = item.groupKey;
        roomKey = item.roomKey;
        name = item.name;
        count = item.count;
        text = item.text;
    }

    // Public instance methods.

}
