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
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.model.Base;

import java.util.List;
import java.util.Map;

/** Provide a Firebase model class representing a chat message, an icon, a name and text. */
public class Message extends Base {

    // Public class constants.

    /** The message types. */
    public final static int SYSTEM = 0;
    public final static int STANDARD = 1;
    // TODO: add this real soon: public final static int PROTECTED = 2;

    // Public instance variables.

    /** The push key of the group to which the message belongs. */
    public String groupKey;

    /** The push key for the room in which the message was created. */
    public String roomKey;

    /** The message text. */
    public String text;

    /** The message type. */
    public int type;

    /** A list of users (by account identifier) in the room, that have not yet seen the message. */
    public List<String> unseenList;

    /** The poster's url. */
    public String url;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    public Message() {
        super();
    }

    /** Build a default Message using all the parameters. */
    public Message(final String key, final String owner, final String name, final long createTime,
                   final String text, final int type, final String url,
                   final List<String> unseenList) {
        super(key, owner, name, createTime);
        this.text = text;
        this.type = type;
        this.url = url;
        this.unseenList = unseenList;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("text", text);
        result.put("type", type);
        result.put("unseenList", unseenList);
        result.put("url", url);
        return result;
    }

    /** Return TRUE iff the message has not been seen by the current account holder. */
    @Exclude public boolean isUnseen() {
        String id = AccountManager.instance.getCurrentAccountId();
        return id != null && unseenList != null && unseenList.contains(id);
    }
}
