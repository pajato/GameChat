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
public class DateHeaderItem {

    // Public enums

    /** Provide an enumeration of the types of views presented in a rooms list. */
    public enum DateHeaderType {
        now (60000L, R.string.now),
        recent (3600000L, R.string.recent),
        today (3600000L * 24, R.string.today),
        yesterday (3600000L * 48, R.string.yesterday),
        thisWeek (3600000L * 24 * 7, R.string.thisWeek),
        lastWeek (3600000L * 24 * 14, R.string.lastWeek),
        thisMonth (3600000L * 24 * 30, R.string.thisMonth),
        lastMonth (3600000L * 24 * 60, R.string.lastMonth),
        thisYear (3600000L * 24 * 365, R.string.thisYear),
        lastYear (3600000L * 24 * 365 * 2, R.string.lastYear),
        old (-1, R.string.old);

        private long mLimit;
        private int mResourceId;

        DateHeaderType(final long limit, final int id) {
            mLimit = limit;
            mResourceId = id;
        }

        public long getLimit() {return mLimit;}

        public int getNameResourceId() {return mResourceId;}
    }

    // Private instance variables.

    /** The item type. */
    private DateHeaderType mType;

    // Public constructors.

    /** Build an instance for a given date entry. */
    public DateHeaderItem(final DateHeaderType type) {
        mType = type;
    }

    // Public instance methods.

    int getNameResourceId() {return mType.getNameResourceId();}
}
