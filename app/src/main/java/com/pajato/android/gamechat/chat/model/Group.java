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

/** Provide a Firebase model class repesenting a chat group, a collection of members and rooms. */
@IgnoreExtraProperties public class Group {

    /** The owning group's push key (group identifier). */
    public String groupKey;

    /** The group name. */
    public String name;

    /** The group member account identifiers. */
    public List<String> memberIdList = new ArrayList<>();

    /** The list of room identifiers in the group. */
    public List<String> roomIdList = new ArrayList<>();

    /** The default constructor. */
    public Group() {}

    /** Build a default Group. */
    public Group(final String groupKey, final String name) {
        this.groupKey = groupKey;
        this.name = name;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("groupKey", groupKey);
        result.put("name", name);
        result.put("memberIdList", memberIdList);
        result.put("roomIdList", roomIdList);

        return result;
    }
}
