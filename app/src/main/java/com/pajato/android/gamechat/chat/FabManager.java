/*
 * Copyright (C) 2016 Pajato Technologies LLC.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see http://www.gnu.org/licenses
 */

package com.pajato.android.gamechat.chat;

import android.support.design.widget.FloatingActionButton;

import com.pajato.android.gamechat.R;


/** Provide a singleton to manage the rooms panel fab button. */
enum FabManager {
    instance;

    /** Provide FAB state constants. */
    enum State {opened, closed}

    // Private class constants.

    // Public instance methods

    /** Initialize the fab button. */
    public void init(final FloatingActionButton fab) {
        // Initialize the fab button state.
        fab.setTag(R.integer.fabStateKey, State.closed);
    }

    /** Toggle the state of the FAB button. */
    public void toggle(final FloatingActionButton fab) {
        // Determine if the fab view STATE tag has a valid state value.
        Object payload = fab.getTag(R.integer.fabStateKey);
        if (payload instanceof State) {
            // It does.  Toggle it by casing on the value.
            State value = (State) payload;
            switch (value) {
                case opened:
                    // Set the icon to +.
                    fab.setImageResource(R.drawable.ic_add_white_24dp);
                    fab.setTag(R.integer.fabStateKey, State.closed);
                    break;
                case closed:
                    // Set the icon to X.
                    fab.setImageResource(R.drawable.ic_clear_white_24dp);
                    fab.setTag(R.integer.fabStateKey, State.opened);
                    break;
            }
        }
    }

}
