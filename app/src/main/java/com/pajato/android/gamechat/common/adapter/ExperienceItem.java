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

import com.pajato.android.gamechat.exp.Experience;

/**
 * Provide a POJO to encapsulate an experience item to be added to a recycler view.
 *
 * @author Paul Michael Reilly
 */
public class ExperienceItem {

    // Public instance variables.

    /** The group key */
    public String groupKey;

    /** The experience (push) key. */
    public String key;

    /** The room key. */
    public String roomKey;

    // Public constructors.

    /** Build an instance for the given group. */
    public ExperienceItem(final Experience experience) {
        // Update the various push keys.
        groupKey = experience.getGroupKey();
        roomKey = experience.getRoomKey();
        key = experience.getExperienceKey();
    }
}
