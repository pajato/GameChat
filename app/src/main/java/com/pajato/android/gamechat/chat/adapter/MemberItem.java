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

/**
 * Provide a POJO to encapsulate a member item to be added to a list view.
 *
 * @author Paul Michael Reilly
 */
public class MemberItem {

    // Public instance variables.

    /** The email address, possibly null. */
    public String email;

    /** The group key. */
    public String groupName;

    /** The member's icon URL, possibly null. */
    public String url;

    /** The member's display name. */
    public String name;

    // Public constructors.

    /** Build an instance using the given group name and account. */
    public MemberItem(final String groupName, final Account account) {
        this.groupName = groupName;
        this.name = account.displayName;
        this.email = account.email;
        this.url = account.url;
    }

}
