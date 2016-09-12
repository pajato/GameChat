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

import com.pajato.android.gamechat.R;

/**
 * Provide a POJO to encapsulate a recycler view list item: either a date label view or a room list
 * view showing the rooms in a group with messages characterized by a preceding date label view.
 *
 * @author Paul Michael Reilly
 */
public class ContactHeaderItem {

    // Public enums

    /** Provide an enumeration of the types of views presented in a rooms list. */
    public enum ContactHeaderType {
        frequent (R.string.frequent),
        contacts (R.string.contacts);

        private int mResourceId;

        ContactHeaderType(final int id) {
            mResourceId = id;
        }

        public int getNameResourceId() {return mResourceId;}
    }

    // Private instance variables.

    /** The item type. */
    private ContactHeaderType mType;

    // Public constructors.

    /** Build an instance for a given date entry. */
    public ContactHeaderItem(final ContactHeaderType type) {
        mType = type;
    }

    // Public instance methods.

    int getNameResourceId() {return mType.getNameResourceId();}
}
