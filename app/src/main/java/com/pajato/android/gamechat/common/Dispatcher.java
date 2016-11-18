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

package com.pajato.android.gamechat.common;

import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.DatabaseListManager;

import java.util.List;
import java.util.Map;

/**
 * The fragment dispatcher provides mediation between the experience or chat managers and the main
 * (envelope, experience or chat) fragment. It captures all information a delegated fragment will
 * need to instantiate and take the foreground.
 *
 * @author Paul Michael Reilly
 */
public class Dispatcher<T, O> {

    // Public instance variables.

    /** The experience or message key. */
    public String key;

    /** The message or experience profile. */
    public O payload;

    /** A list of messages or experience profiles. */
    public List<O> list;

    /** Associates groups, rooms and either messages or experience profiles. */
    public Map<String, Map<String, Map<String, O>>> groupMap;

    /** The group key. */
    public String groupKey;

    /** The room key. */
    public String roomKey;

    /** The map associating a room key with experience profiles in the room. */
    public Map<String, Map<String, O>> roomMap;

    /** The fragment type denoting the fragment index and the experience type. */
    public T type;

    // Public Constructors.

    /** Build an instance given an experience type. */
    public Dispatcher(final T type) {
        this.type = type;
        Room room = DatabaseListManager.instance.getMeRoom();
        if (room != null) {
            groupKey = room.groupKey;
            roomKey = room.key;
        }
    }

    /** Build an instance given a type and an experience map. */
    public Dispatcher(final T type, final Map<String, Map<String, Map<String, O>>> groupMap) {
        this.type = type;
        this.groupMap = groupMap;
    }

    /** Build an instance given a group key and room map. */
    public Dispatcher(final T type, final String key, final Map<String, Map<String, O>> roomMap) {
        this.type = type;
        this.groupKey = key;
        this.roomMap = roomMap;
    }

    /** Build an instance given a group key, room key, and an experience list. */
    public Dispatcher(final T type, final String gKey, final String rKey, final List<O> list) {
        this.type = type;
        this.groupKey = gKey;
        this.roomKey = rKey;
        this.list = list;
        payload = list.size() > 0 ? list.get(0) : null;
    }

    /** Build an instance given a type and a list of message or experience profiles. */
    public Dispatcher(final T type, List<O> list) {
        this.type = type;
        this.list = list;
        payload = list.size() > 0 ? list.get(0) : null;
    }

    /** Build an instance given a fragment type and a message or experience profile. */
    public Dispatcher(final T type, O payload) {
        this.type = type;
        this.payload = payload;
    }
}
