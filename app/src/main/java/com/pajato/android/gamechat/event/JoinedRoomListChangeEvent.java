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

package com.pajato.android.gamechat.event;

import java.util.List;

/**
 * Provides a model class to encapsulate the list of joined rooms to be displayed on the main rooms
 * fragment page.
 *
 * @author Paul Michael Reilly
 */
public class JoinedRoomListChangeEvent {

    // Private instance variables

    /** The list of joined rooms by push key. */
    public List<String> joinedRoomList;

    /** Build the event with the given list. */
    public JoinedRoomListChangeEvent(final List<String> list) {
        joinedRoomList = list;
    }

}
