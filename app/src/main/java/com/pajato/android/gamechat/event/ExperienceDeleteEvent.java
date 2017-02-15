package com.pajato.android.gamechat.event;

import static android.R.attr.key;

/**
 * Provides an event class indicating that an experience has been deleted.
 */
public class ExperienceDeleteEvent {

    // Public instance variables.

    /** The push key for the experience. */
    public final String experienceKey;

    /** Build the event */
    public ExperienceDeleteEvent(final String key) {
        this.experienceKey = key;
    }
}
