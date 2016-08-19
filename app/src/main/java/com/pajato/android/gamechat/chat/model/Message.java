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
import java.util.Map;

/** Provide a Firebase model class repesenting a chat message, an icon, a name and text. */
@IgnoreExtraProperties public class Message {

    /** The message text. */
    public String text;

    /** The display name of the message sender. */
    public String name;

    /** The icon image url for the message sender. */
    public String url;

    /** The default constructor. */
    public Message() {}

    /** Build a default Message. */
    public Message(final String name, final String url, final String text) {
        this.name = name;
        this.url = url;
        this.text = text;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("text", text);
        result.put("url", url);

        return result;
    }
}
