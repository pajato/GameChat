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

import com.pajato.android.gamechat.common.model.Account;

/**
 * Provides an event type indicating that an account state change has just finished being handled.
 *
 * @author Paul Michael Reilly
 */
public class AuthenticationChangeHandled {

    // Public instance variables.

    /** The changed account: if null the User signed out, if non-null the User signed in. */
    public Account account;

    // Public constructor.

    /** Build the instance with the given account; null indicates a sign out occurred. */
    public AuthenticationChangeHandled(final Account account) {
        this.account = account;
    }
}
