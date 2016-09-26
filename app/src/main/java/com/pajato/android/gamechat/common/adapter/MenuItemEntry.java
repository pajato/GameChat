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

package com.pajato.android.gamechat.common.adapter;

/**
 * Provide a POJO to encapsulate a menu item to be added to a recycler view.
 *
 * @author Paul Michael Reilly
 */
public class MenuItemEntry {

    // Public instance variables.

    /** The menu item icon resource id. */
    int iconResId;

    /** The menu item title resource id. */
    int titleResId;

    /** The menu item icon url. */
    String url;

    // Public constructors.

    /** Build an instance for the given title and icon. */
    public MenuItemEntry(final int titleResId, final String url) {
        this.titleResId = titleResId;
        this.url = url;
    }

    /** Build an instance for the given title and icon resource id. */
    public MenuItemEntry(final int titleResId, final int iconResId) {
        this.titleResId = titleResId;
        this.iconResId = iconResId;
    }

}
