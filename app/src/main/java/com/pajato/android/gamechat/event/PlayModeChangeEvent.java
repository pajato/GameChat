package com.pajato.android.gamechat.event;

import android.view.View;

/**
 * Provides an event class indicating that the play mode selection has changed.
 */
public class PlayModeChangeEvent {

    public View view;

    /** Build the event, saving the selected menu item. */
    public PlayModeChangeEvent(View view) {
        this.view = view;
    }
}
