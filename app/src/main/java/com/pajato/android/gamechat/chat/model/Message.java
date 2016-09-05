/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.chat.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provide a Firebase model class repesenting a chat message, an icon, a name and text. */
@IgnoreExtraProperties public class Message {

    /** The member account identifer who posted the message. */
    public String owner;

    /** The message push key. */
    public String messageKey;

    /** The poster's display name. */
    public String name;

    /** The creation timestamp. */
    public long createTime;

    /** The last modification timestamp. */
    public long modTime;

    /** The message text. */
    public String text;

    /** The message type. */
    public String type;

    /** A list of users (by account identifier) in the room, that have not yet read the message. */
    public List<String> unreadList;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    public Message() {}

    /** Build a default Message. */
    public Message(final String owner, final String name, final String messageKey,
                   final long createTime, final long modTime, final String text,
                   final String type, final List<String> unreadList) {
        this.owner = owner;
        this.name = name;
        this.messageKey = messageKey;
        this.createTime = createTime;
        this.modTime = modTime;
        this.text = text;
        this.type = type;
        this.unreadList = unreadList;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("owner", owner);
        result.put("name", name);
        result.put("messageKey", messageKey);
        result.put("createTime", createTime);
        result.put("modTime", modTime);
        result.put("text", text);
        result.put("type", type);
        result.put("unreadList", unreadList);

        return result;
    }
}
