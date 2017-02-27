package com.pajato.android.gamechat.event;

/**
 * Provides an event class indicating that an experience model has been reset.
 */
public class ExperienceResetEvent {

    // Public instance variables.

    /** The push key for the experience. */
    public final String experienceKey;

    /** Build the event */
    public ExperienceResetEvent(final String key) {
        this.experienceKey = key;
    }
}
