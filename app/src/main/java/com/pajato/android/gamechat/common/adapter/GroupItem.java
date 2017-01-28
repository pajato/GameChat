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

package com.pajato.android.gamechat.common.adapter;

import android.support.annotation.NonNull;

import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Provide a POJO to encapsulate a recycler view list item: either a date label view or a room list
 * view showing the rooms in a group with messages characterized by a preceding date label view.
 *
 * @author Paul Michael Reilly
 */
public class GroupItem {

    // Public instance variables.

    /** The group key. */
    public String groupKey;

    /** The item name. */
    public String name;

    /** The number of new messages in the group rooms. */
    public int count;

    /** The list of rooms with messages.  Bold items have new messages. */
    public String text;

    // Public constructors.

    /** Build an instance for the given group. */
    public GroupItem(final String groupKey) {
        // Use the group key to unpack a group's messages by walking the set of messages in
        // each room in the group.
        this.groupKey = groupKey;
        count = 0;
        StringBuilder textBuilder = new StringBuilder();
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        name = group != null ? group.name : "";
        Map<String, Integer> roomMap = new HashMap<>();
        Map<String, Map<String, Message>> roomMessageMap;
        roomMessageMap = GroupManager.instance.getGroupMessages(groupKey);
        for (String roomKey : roomMessageMap.keySet()) {
            int roomNewCount = 0;
            for (Message message : roomMessageMap.get(roomKey).values()) {
                if (isUnseen(message)) {
                    roomNewCount++;
                    count++;
                }
                roomMap.put(roomKey, roomNewCount);
            }
        }

        // Update the list of rooms
        for (String roomKey : roomMap.keySet()) {
            Room room = RoomManager.instance.getRoomProfile(roomKey);
            update(textBuilder, room, roomMap.get(roomKey) > 0);
        }
    }

    // Private instance methods.

    /** Return TRUE iff the message has not been seen by this user. */
    private boolean isUnseen(@NonNull final Message message) {
        String accountId = AccountManager.instance.getCurrentAccountId();
        return message.unreadList != null && message.unreadList.contains(accountId);
    }

    /** Update the text indicating the rooms with messages bolding rooms with new messages. */
    private void update(final StringBuilder textBuilder, final Room room, final boolean hasNew) {
        // Determine if there is already text generated.
        if (textBuilder.length() != 0) {
            // There is.  Add a comma and space.
            textBuilder.append(", ");
        }

        // Determine if bolding is needed and update the field.
        if (hasNew) {
            textBuilder.append("<b>").append(room.getName()).append("</b>");
        } else {
            textBuilder.append(room.getName());
        }
        text = textBuilder.toString();
    }
}