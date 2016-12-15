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

import com.pajato.android.gamechat.chat.model.Account;

import java.util.Locale;

/**
 * Provides a model class to encapsulate the list of joined rooms to be displayed on the main rooms
 * fragment page.
 *
 * @author Paul Michael Reilly
 */
public class MemberChangeEvent {

    /** The group push key associated with the account. */
    public String groupKey;

    /** The account push key (User id) */
    public String key;

    /** The changed, non-null acount. */
    public Account member;

    /** Build the instance with the given account; null indicates a sign out occurred. */
    public MemberChangeEvent(final String key, final String groupKey, final Account member) {
        this.key = key;
        this.groupKey = groupKey;
        this.member = member;
    }

    /** Return the description of the instance. */
    public String toString() {
        String format = "Member id: {%s}, group id: {%s}, member: {%s}";
        return String.format(Locale.US, format, key, groupKey, member.toMap());
    }
}
