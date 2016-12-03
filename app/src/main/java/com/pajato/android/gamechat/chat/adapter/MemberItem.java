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

import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.database.DatabaseListManager;

/**
 * Provide a POJO to encapsulate a member item to be added to a list view.
 *
 * @author Paul Michael Reilly
 */
public class MemberItem {

    // Public instance variables.

    /** The group key. */
    public String groupKey;

    /** The member's display name. */
    public String name;

    /** The member's nickname. */
    public String text;

    /** The member's icon URL, possibly null. */
    public String url;

    // Public constructors.

    /** Build an instance using the given group name and account. */
    public MemberItem(final String groupKey, final Account member) {
        this.groupKey = groupKey;
        this.name = member.getNickName("Anonymous");
        Group group = DatabaseListManager.instance.getGroupProfile(groupKey);
        text = group.name;
        this.url = member.url;
    }
}