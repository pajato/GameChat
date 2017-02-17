/*
 * Copyright (C) 2017 Pajato Technologies LLC.
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
package com.pajato.android.gamechat.database.handler;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ProtectedUserChangeEvent;

/**
 * Handle changes to a protected user account by posting an app event.
 */

public class ProtectedUserChangeHandler extends DatabaseEventHandler implements ValueEventListener {

    /** The logcat TAG. */
    private static final String TAG = ProtectedUserChangeHandler.class.getSimpleName();

    /** Build a handler with the given name, path and key. */
    public ProtectedUserChangeHandler (final String name, final String path) {
        super(name, path);
    }

    /** Get the current account using a list of account identifiers. */
    @Override public void onDataChange(final DataSnapshot dataSnapshot) {
        // Determine if the account exists.
        Account account;
        if (dataSnapshot.exists()) {
            // It does.  Register it and notify the app that this is the new account of record.
            account = dataSnapshot.getValue(Account.class);
            AppEventManager.instance.post(new ProtectedUserChangeEvent(account));
        }
    }

    /** ... */
    @Override public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }

}
