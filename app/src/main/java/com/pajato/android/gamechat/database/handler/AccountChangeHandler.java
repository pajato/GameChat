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

package com.pajato.android.gamechat.database.handler;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.event.AccountChangeEvent;
import com.pajato.android.gamechat.event.AppEventManager;

import java.util.Date;

/**
 * Provide a class to handle changes to an account by posting an app event.
 *
 * @author Paul Michael Reilly
 */
public class AccountChangeHandler extends DatabaseEventHandler implements ValueEventListener {

    // Private class constants.

    /** The logcat TAG. */
    private static final String TAG = AccountChangeHandler.class.getSimpleName();

    // Public constructors.

    /** Build a handler with the given name, path and key. */
    public AccountChangeHandler(final String name, final String path) {
        super(name, path);
    }

    /** Get the current account using a list of account identifiers. */
    @Override public void onDataChange(final DataSnapshot dataSnapshot) {
        // Determine if the account exists.
        Account account;
        if (dataSnapshot.exists()) {
            // It does.  Register it and notify the app that this is the new account of record.
            account = dataSnapshot.getValue(Account.class);
            AppEventManager.instance.post(new AccountChangeEvent(account));
        } else {
            // The account does not exist.  Create it now, ensuring there really is a User.
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            account = user != null ? getAccount(user) : null;
            if (account != null) AccountManager.instance.createAccount(account);
        }
    }

    /** ... */
    @Override public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }

    // Private instance methods.

    /** Return a partially populated account. The database manager will finish the job. */
    private Account getAccount(final FirebaseUser user) {
        long tStamp = new Date().getTime();
        Account account = new Account();
        account.id = user.getUid();
        account.email = user.getEmail();
        account.displayName = user.getDisplayName();
        account.url = getPhotoUrl(user);
        account.providerId = user.getProviderId();
        account.type = AccountManager.AccountType.standard.name();
        account.createTime = tStamp;
        return account;
    }

    /** Obtain a suitable Uri to use for the User's icon. */
    private String getPhotoUrl(FirebaseUser user) {
        // TODO: figure out how to handle a generated icon ala Inbox, GMail and Hangouts.
        Uri icon = user.getPhotoUrl();
        return icon != null ? icon.toString() : null;
    }

}
