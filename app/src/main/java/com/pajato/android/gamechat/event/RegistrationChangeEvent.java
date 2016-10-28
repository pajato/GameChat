package com.pajato.android.gamechat.event;

import com.pajato.android.gamechat.chat.model.Room;

/**
 * Provides an event class to encapsulate a registration change event for a given class name.
 *
 * @author Paul Michael Reilly
 */
public class RegistrationChangeEvent {

    // Public constants.

    /** The change type value for a registered event handler. */
    public final static int REGISTERED = 1;

    /** The change type value for an unregistered event handler. */
    public final static int UNREGISTERED = 2;

    // Public instance variables.

    /** The type of registration change: either registered or unregistered. */
    public final int changeType;

    /** The fully qualified class name. */
    public final String name;

    // Public constructor.

    /** Build the event for the generic type and it's push key. */
    public RegistrationChangeEvent(final String name, final int changeType) {
        this.name = name;
        this.changeType = changeType;
    }

}
