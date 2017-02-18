/*
 * Copyright (C) 2017 Pajato Technologies, Inc.
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
 * Provides a protected user account deleted event.
 */
public class ProtectedUserDeleteEvent {

    // Public instance variable.

    /** The changed, non-null account. */
    public Account account;

    // Public constructor.

    /** Build the instance with the given account; should represent a protected user! */
    public ProtectedUserDeleteEvent(final Account account) {
        this.account = account;
    }
}
