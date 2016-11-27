/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.event;

import com.pajato.android.gamechat.account.Account;

/**
 * Provides an account change data model class.
 *
 * @author Paul Michael Reilly
 */
public class AccountChangeEvent {

    // Public instance variables

    /** The group push key associated with the account. */
    public String groupKey;

    /** The account push key (User id) */
    public String key;

    /** The changed, non-null acount. */
    public Account account;

    /** Build the instance with the given account; null indicates a sign out occurred. */
    public AccountChangeEvent(final String key, final String groupKey, final Account account) {
        this.key = key;
        this.account = account;
    }
}
