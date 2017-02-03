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

import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.database.GroupManager;

import java.util.Locale;

/**
 * Provide a POJO to encapsulate a member item to be added to a list view.
 *
 * @author Paul Michael Reilly
 */
public class UserItem {

    // Public instance variables.

    /** The group key. */
    public String groupKey;

    /** The possibly empty (null) group member push key. */
    public String memberKey;

    /** The member's display name. */
    public String name;

    /** The selection state. */
    public boolean selected;

    /** The member's nickname. */
    public String text;

    /** The member's icon URL, possibly null. */
    public String url;

    // Public constructors.

    /** Build an instance using the given group name and account. */
    public UserItem(final String groupKey, final Account member) {
        this.groupKey = groupKey;
        this.memberKey = member.id;
        this.name = String.format(Locale.US, "%s (%s)", member.getNickName(), member.email);
        this.url = member.url;
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        text = group != null ? group.name : null;
        this.url = member.url;
    }
}
