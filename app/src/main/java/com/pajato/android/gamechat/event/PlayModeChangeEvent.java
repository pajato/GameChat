package com.pajato.android.gamechat.event;

import com.pajato.android.gamechat.common.PlayModeManager.PlayModeType;

/**
 * Provides an event class indicating that the play mode selection has changed.
 */
public class PlayModeChangeEvent {

    public PlayModeType type;

    /** Build the event, saving the selected menu item. */
    public PlayModeChangeEvent(PlayModeType type) {
        this.type = type;
    }

}
