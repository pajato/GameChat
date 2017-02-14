package com.pajato.android.gamechat.event;

/**
 * Provides an event class indicating that a group profile has been deleted.
 */

public class ProfileGroupDeleteEvent {

    // Public instance variables.

    /** The push key for the group. */
    public final String groupKey;

    /** Build the event */
    public ProfileGroupDeleteEvent(final String key) {
        this.groupKey = key;
    }
}
