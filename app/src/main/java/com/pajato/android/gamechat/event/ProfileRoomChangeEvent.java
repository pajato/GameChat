package com.pajato.android.gamechat.event;

import com.pajato.android.gamechat.chat.model.Room;

/**
 * Provides an event class to encapsulate the profile for a room model class.
 *
 * @author Paul Michael Reilly
 */
public class ProfileRoomChangeEvent {

    // Public instance variables.

    /** The push key for the group. */
    public final Room room;

    /** The push key for the group. */
    public final String key;

    /** Build the event for the generic type and it's push key. */
    public ProfileRoomChangeEvent(final String key, final Room room) {
        this.key = key;
        this.room = room;
    }

}
