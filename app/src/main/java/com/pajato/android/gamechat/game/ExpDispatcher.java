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

package com.pajato.android.gamechat.game;

import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.game.model.ExpProfile;

import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.game.ExpFragmentType.expList;
import static com.pajato.android.gamechat.game.ExpFragmentType.groupList;
import static com.pajato.android.gamechat.game.ExpFragmentType.roomList;

/**
 * The experience dispatcher provides mediation between the experience (game) manager and the main
 * (envelope) experience fragment. It captures all information a delegated fragment will need to
 * instantiate and take the foreground.
 *
 * @author Paul Michael Reilly
 */
public class ExpDispatcher {

    // Public instance variables.

    /** The experience key. */
    public String expKey;

    /** The one and only experience profile. */
    public ExpProfile expProfile;

    /** A list of experience profiles. */
    public List<ExpProfile> profileList;

    /** Associates groups, rooms and experience profiles. */
    public Map<String, Map<String, Map<String, ExpProfile>>> groupMap;

    /** The group key. */
    public String groupKey;

    /** The room key. */
    public String roomKey;

    /** The map associating a room key with experience profiles in the room. */
    public Map<String, Map<String, ExpProfile>> roomMap;

    /** The fragment type denoting the fragment index and the experience type. */
    public ExpFragmentType type;

    // Public Constructors.

    /** Build an instance given an experience type. */
    ExpDispatcher(final ExpFragmentType type) {
        this.type = type;
        Room room = DatabaseListManager.instance.getMeRoom();
        if (room != null) {
            groupKey = room.groupKey;
            roomKey = room.key;
        }
    }

    /** Build an instance given an experience map. */
    ExpDispatcher(final Map<String, Map<String, Map<String, ExpProfile>>> groupMap) {
        this.type = groupList;
        this.groupMap = groupMap;
    }

    /** Build an instance given a group key and room map. */
    ExpDispatcher(final String groupKey, final Map<String, Map<String, ExpProfile>> roomMap) {
        this.type = roomList;
        this.groupKey = groupKey;
        this.roomMap = roomMap;
    }

    /** Build an instance given a group key, room key, and an experience list. */
    ExpDispatcher(final String groupKey, final String roomKey, final List<ExpProfile> profileList) {
        this.type = expList;
        this.groupKey = groupKey;
        this.roomKey = roomKey;
        this.profileList = profileList;
    }

    /** Build an instance given a fragment type, a group key, a room key and an experience key. */
    ExpDispatcher(final ExpFragmentType type, final String group, final String room, final String key) {
        this.type = type;
        groupKey = group;
        roomKey = room;
        expKey = key;
    }

    /** Build an instance given a fragment type and a list of experience keys. */
    ExpDispatcher(final ExpFragmentType type, List<ExpProfile> profileList) {
        this.type = type;
        this.profileList = profileList;
    }

    /** Build an instance given a fragment type and an experiene profile. */
    ExpDispatcher(final ExpFragmentType type, ExpProfile expProfile) {
        this.type = type;
        this.expProfile = expProfile;
    }

}
