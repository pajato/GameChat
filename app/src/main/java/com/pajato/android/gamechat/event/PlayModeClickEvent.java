package com.pajato.android.gamechat.event;

import android.view.View;

/**
 * Provides an event class indicating that the a play mode menu click has occurred.
 */
public class PlayModeClickEvent {

    public View view;

    /** Build the event, saving the selected view */
    public PlayModeClickEvent(View view) {
        this.view = view;
    }
}
