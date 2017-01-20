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

package com.pajato.android.gamechat.chat.adapter;

/**
 * Provide a POJO to encapsulate a general heading item containing text from a given string
 * resource.
 *
 * @author Paul Michael Reilly
 */
public class ResourceHeaderItem {

    // Private instance variables.

    /** The item resourceId. */
    private int mResourceId;

    // Public constructors.

    /** Build an instance for a given resource identifier. */
    public ResourceHeaderItem(final int resourceId) {
        mResourceId = resourceId;
    }

    // Public instance methods.

    /** Return the reource id. */
    int getNameResourceId() {
        return mResourceId;
    }
}
