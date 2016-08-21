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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provide a Firebase model class for representing a chat room. */
@IgnoreExtraProperties public class Room {

    /** The room owner's push key (group identifier). */
    public String groupKey;

    /** The room name. */
    public String name;

    /** The room member account identifiers. These are the people currently in the room. */
    public List<String> memberIdList = new ArrayList<>();

    /** The list of message identifiers in the room. */
    public List<String> messageIdList = new ArrayList<>();

    /** The default constructor. */
    public Room() {}

    /** Build a default room. */
    public Room(final String groupKey, final String name) {
        this.groupKey = groupKey;
        this.name = name;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("gruopKey", groupKey);
        result.put("name", name);
        result.put("memberIdList", memberIdList);
        result.put("messageIdList", messageIdList);

        return result;
    }
}
