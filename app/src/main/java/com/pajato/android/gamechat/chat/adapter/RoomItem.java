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

import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provide a POJO to encapsulate a recycler view list item: either a date label view or a room list
 * view showing the rooms in a group with messages characterized by a preceding date label view.
 *
 * @author Paul Michael Reilly
 */
public class RoomItem {

    // Public instance variables.

    /** The group key */
    public String groupKey;

    /** The room key. */
    public String roomKey;

    /** The room name. */
    public String name;

    /** The number of new messages in the group rooms. */
    int count;

    /** The list of group members with messages.  Bold items are associated with new messages. */
    String text;

    // Public constructors.

    /** Build an instance for the given group. */
    public RoomItem(final String groupKey, final String roomKey) {
        // Use the group key to unpack a group's messages by walking the set of messages in
        // each room in the group.
        this.groupKey = groupKey;
        this.roomKey = roomKey;
        count = 0;
        String accountId = AccountManager.instance.getCurrentAccountId();
        StringBuilder textBuilder = new StringBuilder();
        Map<String, Boolean> memberNameMap = new HashMap<>();
        Room room = DatabaseListManager.instance.getRoomProfile(roomKey);
        name = room.name;
        Map<String, List<Message>> rooms = DatabaseListManager.instance.getGroupMessages(groupKey);
        for (Message message : rooms.get(roomKey)) {
            // Ensure that the member who posted the message is in the member display name map.
            String displayName = message.owner.equals(accountId) ? "me" : message.name;
            if (!memberNameMap.containsKey(displayName)) memberNameMap.put(displayName, false);
            if (message.unreadList != null && message.unreadList.contains(accountId)) {
                memberNameMap.put(displayName, true);
                count++;
            }
        }

        // Update the members text field by walking the members name map.
        for (String displayName : memberNameMap.keySet()) {
            // Determine if there is already text generated.
            if (textBuilder.length() != 0) {
                // There is.  Add a comma and space.
                textBuilder.append(", ");
            }

            // Determine if bolding is needed and update the field.
            if (memberNameMap.get(displayName)) {
                // Bolding is needed.
                textBuilder.append("<b>").append(displayName).append("</b>");
            } else {
                textBuilder.append(displayName);
            }
            text = textBuilder.toString();
        }
    }

}
