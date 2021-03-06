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

import com.pajato.android.gamechat.chat.model.Group;

/**
 * Provides an event class to encapsulate the profile for a generic model class.
 *
 * @author Paul Michael Reilly
 */
public class ProfileGroupChangeEvent {

    // Public instance variables.

    /** The push key for the group. */
    public final Group group;

    /** The push key for the group. */
    public final String key;

    /** Build the event for the generic type and it's push key. */
    public ProfileGroupChangeEvent(final String key, final Group group) {
        this.key = key;
        this.group = group;
    }

}
