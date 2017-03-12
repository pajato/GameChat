/*
 * Copyright (C) 2017 Pajato Technologies LLC.
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

import java.util.Locale;

/**
 * Provide a POJO to encapsulate a recycler view list item for use in the player mode menu.
 */

public class PlayModeMenuEntry {

    // Type constants
    final static int MENU_TEXT_TYPE = 100;

    /** The associated account push key, or null if not a user entry */
    public String accountKey;

    /** The group key associated with the target member, if any */
    public String groupKey;

    /** A description of the item. */
    private String mDescription;

    /** The entry type, provided by the item. */
    public int type;

    /** The text string used for menu display */
    public String title;

    /** Build an instance for a given menu item. */
    public PlayModeMenuEntry(String text, String accountKey, String groupKey) {
        this.type = MENU_TEXT_TYPE;
        this.accountKey = accountKey;
        this.groupKey = groupKey;
        this.title = text;
        String format = "Play Mode Menu item with title {%s}, account id {%s} and group key {%s}.";
        mDescription = String.format(Locale.US, format, title, accountKey, groupKey);
    }

    // Public instance methods.

    /** Return a description of the object. */
    @Override public String toString() {
        return mDescription;
    }
}
