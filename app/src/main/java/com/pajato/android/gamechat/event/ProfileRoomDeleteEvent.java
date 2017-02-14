package com.pajato.android.gamechat.event;

/**
 * Provides an event class indicating that a room profile has been deleted.
 */

public class ProfileRoomDeleteEvent {

    // Public instance variables.

    /** The push key for the room. */
    public final String roomKey;

    /** Build the event */
    public ProfileRoomDeleteEvent(final String key) {
        this.roomKey = key;
    }
}


