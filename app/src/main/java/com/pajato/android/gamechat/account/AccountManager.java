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

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.signin.SignInActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the account related aspects of the GameChat application.  These include setting up the
 * first time sign-in result, creating and persisting a profile on this device and switching
 * accounts.
 *
 * @author Paul Michael Reilly
 */
public enum AccountManager implements FirebaseAuth.AuthStateListener {
    instance;

    public enum Actions {signIn, signOut}

    /** A key used to access account available data. */
    public static final String ACCOUNT_AVAILABLE_KEY = "accountAvailable";

    // Private class constants

    // Private instance variables

    /** The account repository associating mulitple account id strings with the cloud account. */
    private Map<String, Account> mAccountMap = new HashMap<>();

    /** The array of click keys.  The ... */
    private SparseArray<Actions> mActionMap = new SparseArray<>();

    // Public instance methods

    /** Retrun the account for the current User, null if there is no signed in User. */
    public Account getCurrentAccount() {
        // Obtain the account key from the logged in Firebase User to use to lookup the account in
        // the account map.  If the account is not in the map, load it using the Firebase User data.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : null;
        Account account = uid != null && mAccountMap.containsKey(uid) ? mAccountMap.get(uid) : null;
        return uid == null || account != null ? account : getAccount(user);
    }

    /** Deal with authentication backend changes: sign in and sign out */
    @Override public void onAuthStateChanged(@NonNull final FirebaseAuth auth) {
        // Determine if the User is signed in or out.
        FirebaseUser user = auth.getCurrentUser();
        Account account = user != null ? getCurrentAccount() : null;
        EventBus.getDefault().post(new AccountStateChangeEvent(account));
    }

    /** Initialize the account manager. */
    public void init() {
        // Build a sparse array associating auth events by resource id (User clicked in a sign in or
        // sign out button) with an auth action.
        mActionMap.put(R.id.signIn, Actions.signIn);
        mActionMap.put(R.id.signOut, Actions.signOut);
    }

    /** Register the component during lifecycle resume events. */
    public void register() {
        // Deal with auth sign in and sign out events by propagating them to EventBus subscribers.
        EventBus.getDefault().register(this);
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    /** Unregister the component during lifecycle pause events. */
    public void unregister() {
        EventBus.getDefault().unregister(this);
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    /** Handle a sign in or sign out button click. */
    @Subscribe public void processClick(final ClickEvent event) {
        // Case on the view's tag content.
        Actions action = mActionMap.get(event.getValue());
        if (action != null) {
            processAction(event, action);
        }
    }

    // Private instance methods.

    /** Map the Firebase User to an Account. */
    private Account getAccount(final FirebaseUser user) {
        // Create and persist an account for the user.
        Account result = new Account();
        result.accountId = user.getUid();
        result.accountEmail = user.getEmail();
        result.accountUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
        result.displayName = user.getDisplayName();
        result.providerId = user.getProviderId();
        result.providerName = "tbd";
        mAccountMap.put(result.accountId, result);

        return result;
    }

    /** Process a given action by providing handling for the relevant cases. */
    private void processAction(final ClickEvent event, final Actions action) {
        switch (action) {
            case signIn:
                // Invoke the sign in activity to kick off a Firebase auth event.
                Context context = event.getContext();
                Intent intent = new Intent(context, SignInActivity.class);
                intent.putExtra("signin", true);
                context.startActivity(intent);
                break;
            case signOut:
                // Have Firebase log out the user.
                FirebaseAuth.getInstance().signOut();
                break;
            default:
                break;
        }
    }

    // Private classes

}
