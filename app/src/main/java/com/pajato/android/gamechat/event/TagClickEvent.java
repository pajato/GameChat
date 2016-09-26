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

import android.view.View;

/**
 * Provides a button click data model class where the payload is in the view tag field.
 *
 * @author Paul Michael Reilly
 */
public class TagClickEvent {

    // Private instance variables.

    /** The view associated with the click event, if any. */
    public View view;

    // Public constructors.

    /** Build the event with the given view. */
    public TagClickEvent(final View view) {
        this.view = view;
    }

}
