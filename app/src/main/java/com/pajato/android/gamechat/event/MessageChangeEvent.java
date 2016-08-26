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

import com.pajato.android.gamechat.chat.model.Message;

/**
 * Provides a event class to encapsulate a message class along with the parent group and room keys.
 *
 * @author Paul Michael Reilly
 */
public class MessageChangeEvent {

    // Public instance variables.

    /** The push key for the group containing the message. */
    public final String groupKey;

    /** The push key for the room containing the message. */
    public final String roomKey;

    /** The value associated with the click event, either a tag value of the reoource id. */
    public Message message;

    /** Build the event with the given list. */
    public MessageChangeEvent(final String groupKey, final String roomKey, final Message message) {
        this.groupKey = groupKey;
        this.roomKey =  roomKey;
        this.message = message;
    }

}
