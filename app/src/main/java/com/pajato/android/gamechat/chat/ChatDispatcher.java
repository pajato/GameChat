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

package com.pajato.android.gamechat.chat;

import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.game.model.ExpProfile;

import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.chat.ChatFragmentType.groupList;
import static com.pajato.android.gamechat.chat.ChatFragmentType.roomList;

/**
 * The experience dispatcher provides mediation between the experience (game) manager and the main
 * (envelope) experience fragment. It captures all information a delegated fragment will need to
 * instantiate and take the foreground.
 *
 * @author Paul Michael Reilly
 */
public class ChatDispatcher {

    // Public instance variables.

    /** The message key. */
    public String messageKey;

    /** The one and only experience profile. */
    public Message message;

    /** A list of experience profiles. */
    public List<Message> messageList;

    /** Associates groups, rooms and experience profiles. */
    public Map<String, Map<String, Map<String, Message>>> groupMap;

    /** The group key. */
    public String groupKey;

    /** The room key. */
    public String roomKey;

    /** The map associating a room key with experience profiles in the room. */
    public Map<String, Map<String, Message>> roomMap;

    /** The fragment type. */
    public ChatFragmentType type;

    // Public Constructors.

    /** Build an instance given a fragment type. */
    ChatDispatcher(final ChatFragmentType type) {
        this.type = type;
        Room room = DatabaseListManager.instance.getMeRoom();
        if (room != null) {
            groupKey = room.groupKey;
            roomKey = room.key;
        }
    }

    /** Build an instance given a group map. */
    ChatDispatcher(final Map<String, Map<String, Map<String, Message>>> groupMap) {
        this.type = groupList;
        this.groupMap = groupMap;
    }

    /** Build an instance given a group key and room map. */
    ChatDispatcher(final String groupKey, final Map<String, Map<String, Message>> roomMap) {
        this.type = roomList;
        this.groupKey = groupKey;
        this.roomMap = roomMap;
    }

    /** Build an instance given a group key, room key, and a message list. */
    ChatDispatcher(final String groupKey, final String roomKey, final List<Message> messageList) {
        this.type = ChatFragmentType.messageList;
        this.groupKey = groupKey;
        this.roomKey = roomKey;
        this.messageList = messageList;
    }

    /** Build an instance given a fragment type, a group key, a room key and a message key. */
    ChatDispatcher(final ChatFragmentType type, final String group, final String room,
                   final String key) {
        this.type = type;
        groupKey = group;
        roomKey = room;
        messageKey = key;
    }

    /** Build an instance given a fragment type and a list of messages. */
    ChatDispatcher(final ChatFragmentType type, List<Message> messageList) {
        this.type = type;
        this.messageList = messageList;
    }

    /** Build an instance given a fragment type and a message. */
    ChatDispatcher(final ChatFragmentType type, Message message) {
        this.type = type;
        this.message = message;
    }
}
