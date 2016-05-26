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
package com.pajato.android.gamechat.account;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the account related aspects of the GameChat application.  These include setting up the
 * first time sign-in, creating a persona (nickname and avatar), switching accounts and personas,
 * ...
 *
 * @author Paul Michael Reilly
 */
public enum AccountManager {
    instance;

    // Private class constants

    /** The logcat logger tag. */
    private static final String TAG = AccountManager.class.getSimpleName();

    // Local storage keys.
    private static final String KEY_ACCOUNT_NAME = "keyAccountName";
    private static final String KEY_ACCOUNT_DISPLAY_NAME = "keyAccountDisplayName";
    private static final String KEY_ACCOUNT_TYPE = "keyAccountType";
    private static final String KEY_ACCOUNT_URL = "keyAccountUrl";
    private static final String KEY_ACCOUNT_TOKEN = "keyAccountToken";

    // Activity request codes.
    private static final int ACCOUNTS_PERMISSION_REQUEST = 1;
    private static final int ACCOUNT_SETUP_REQUEST = 2;

    // Private instance variables

    /** The account repository associating mulitple account id strings with the cloud account. */
    private ConcurrentHashMap<String, Account> mAccountMap = new ConcurrentHashMap<>();

    // Public instance methods

    /**
     * Override to implement.
     *
     * @see com.pajato.android.gamechat.account.AccountManager#hasAccount()
     */
    public boolean hasAccount() {
        // The account is considered missing if there is no value with an associated key in the map.
        return mAccountMap.size() > 0;
    }

    /** Register a given account and provider ids. */
    public void register(final String accountId) {
        // Perform the Firebase authentication thing using the given ids.
    }

    // Private instance methods.

    // Private classes

}
