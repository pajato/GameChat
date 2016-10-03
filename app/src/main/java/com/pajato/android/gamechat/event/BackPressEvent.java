/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.event;

import android.app.Activity;

/**
 * Provides a back button event class. One of these is posted on a back press event.
 *
 * @author Paul Michael Reilly
 */
public class BackPressEvent {

    // Public instance variables.

    /** The activity detecting the event. */
    public Activity activity;

    // Public constructors.

    /** Build the instance with a given activity. */
    public BackPressEvent(final Activity activity) {
        this.activity = activity;
    }

}