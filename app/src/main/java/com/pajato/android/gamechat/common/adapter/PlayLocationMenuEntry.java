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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provide a POJO to encapsulate a recycler view list item for use in the player mode menu.
 */
public class PlayLocationMenuEntry {

    // Type constants
    final static int MENU_TEXT_TYPE = 500;

    /** Group keys list, usually contains one entry, except when a member belongs to > 1 group */
    public List<String> groupKeyList;

    /** The associated room key */
    public String roomKey;

    /** A description of the item. */
    private String mDescription;

    /** The entry type, provided by the item. */
    public int type;

    /** The text string used for menu display */
    public String title;

    /** Build an instance for a given menu item. */
    public PlayLocationMenuEntry(String text, String groupKey, String roomKey) {
        this.type = MENU_TEXT_TYPE;
        this.groupKeyList = new ArrayList<>();
        this.groupKeyList.add(groupKey);
        this.roomKey = roomKey;
        this.title = text;
        String format = "Play Location Menu item with title {%s}, group key {%s}, room key {%s}";
        mDescription = String.format(Locale.US, format, title, groupKey, roomKey);
    }

    // Public instance methods.

    /** Return a description of the object. */
    @Override public String toString() {
        return mDescription;
    }
}
