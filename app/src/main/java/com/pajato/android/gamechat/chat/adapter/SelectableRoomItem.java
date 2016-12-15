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

import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;

/**
 * Provide a POJO to encapsulate a recycler view list item: one that allows rooms to be selected for
 * joining.
 *
 * @author Paul Michael Reilly
 */
public class SelectableRoomItem {

    // Public instance variables.

    /** The group key */
    public String groupKey;

    /** The room key. */
    public String roomKey;

    /** The room name. */
    public String name;

    /** The list of group members with messages.  Bold items are associated with new messages. */
    String text;

    // Public constructors.

    /** Build an instance for the given group. */
    public SelectableRoomItem(final String groupKey, final String roomKey) {
        // Generate the name value (the room name) and the text value (the group name).
        this.groupKey = groupKey;
        this.roomKey = roomKey;
        Room room = RoomManager.instance.getRoomProfile(roomKey);
        name = room.name;
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        text = group != null ? group.name : "";
    }
}
