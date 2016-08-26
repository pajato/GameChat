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
public class RoomsListItem {

    // Public constants.

    public static final int DATE_ITEM_TYPE = 0;
    public static final int GROUP_ITEM_TYPE= 1;

    // Public enums

    // Public instance variables.

    /** The item type. */
    public int type;

    /** The item name, possibly null. */
    public String name;

    /** The item name resource identifier. */
    public int nameResourceId;

    /** The group list item count of new messages. */
    public int newCount;

    /** The list of rooms with messages to show in a group. */
    public String roomsText;

    // Public constructors.

    /** Build an instance for a given group list item. */
    public RoomsListItem(final GroupListItem item) {
        type = GROUP_ITEM_TYPE;
        name = item.name;
        newCount = item.newMessageCount;
        roomsText = item.roomsText;
    }

    /** Build an instance for a given date header item. */
    public RoomsListItem(final DateHeaderItem item) {
        type = DATE_ITEM_TYPE;
        nameResourceId = item.getNameResourceId();
        // TODO: flesh this out to encapsulate enough information to build the corresponding view.
    }

    // Public instance methods.

}
