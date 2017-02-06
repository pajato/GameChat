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

import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.database.GroupManager;

/**
 * Provide a POJO to encapsulate a recycler view list item: one that allows a group to be
 * selected.
 */

public class SelectableGroupItem {

    // public instance variables

    /** the group key */
    public String groupKey;

    /** the group name */
    public String name;

    /** Build an instance for a given group */
    public SelectableGroupItem(final String groupKey) {
        this.groupKey = groupKey;
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        name = group != null ? group.name : "";
    }

}
