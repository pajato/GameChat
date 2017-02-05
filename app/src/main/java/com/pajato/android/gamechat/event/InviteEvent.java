package com.pajato.android.gamechat.event;

/**
 * Provides an event type indicating that a group invitation should be initiated.
 */

public class InviteEvent {

    /** Enum value indicating the type of item for the invitation */
    public enum ItemType {
        room,
        group
    }

    // Public instance variables.

    /** A key indicating an item to which an invitation should be issued */
    public String key;

    /** A room to which an invitation should be issued (optional) */
    public ItemType type;

    // Public constructor.

    /** Build an instance. */
    public InviteEvent(final String key, ItemType type) {
        this.key = key;
        this.type = type;
    }

}
