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

import com.pajato.android.gamechat.database.model.Base;

import java.util.List;
import java.util.Map;

/** Provide a Firebase model class representing a chat group, a collection of members and rooms. */
public class Group extends Base {

    /** The key for the group's common room */
    public String commonRoomKey;

    /** The map associating a member name with the a push key. */
    public List<String> memberList;

    /** The list of room push keys in the group. */
    public List<String> roomList;

    /** Build an empty args constructor for the database. */
    public Group() {
        super();
    }

    /** Build a default Group. */
    public Group(final String key, final String owner, final String name, final long createTime,
                 final List<String> members, final List<String> rooms) {
        super(key, owner, name, createTime);
        memberList = members;
        roomList = rooms;
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("commonRoomKey", commonRoomKey);
        result.put("memberList", memberList);
        result.put("roomList", roomList);
        return result;
    }
}
