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

package com.pajato.android.gamechat.common;

/** Provide a singleton to manage the rooms panel fab button. */
enum ToolbarManager {
    instance;

    // Public enums

    /** The toolbar types. */
    enum ToolbarType {
        chatMain(),
        chatChain(),
        create(),
        join(),
        expMain(),
        expChain();

        // Instance variables.

        /** The overflow menu icon resource id. */
        int overflowMenuIconResourceId;

        /** The overflow menu resource id. */
        int overflowMenuResourceId;

        /** The navigation icon resource id. */
        int navigationIconResourceId;

        /** The toolbar title resource id. */
        int titleResourceId;

        // Constructors.

        /** Build a default instance. */
        ToolbarType() {}
    }

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ToolbarManager.class.getSimpleName();

    // Private instance variables.

    // Sole Constructor.

    /** Build the instance with the given resource ids. */
    ToolbarManager() {}

    // Public instance methods

    // Private instance methods.
}
