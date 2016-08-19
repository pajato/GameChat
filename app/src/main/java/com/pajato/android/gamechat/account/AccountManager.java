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
import android.util.Log;
import android.util.SparseArray;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    /** The account value change listener that is managed during activity lifecycle events. */
    private ValueEventListener mAccountChangeHandler;

    // Public instance methods

    /** Retrun the account for the current User, null if there is no signed in User. */
    public Account getCurrentAccount() {
        // Use the default authentication object.
        return getCurrentAccount(FirebaseAuth.getInstance());
    }

    /** Return the account for the current User for a given auth insntance. */
    public Account getCurrentAccount(final FirebaseAuth auth) {
        // Obtain the account key from the logged in Firebase User to use to lookup the account in
        // the account map.  If the account is not in the map, load it using the Firebase User data.
        FirebaseUser user = auth.getCurrentUser();
        String uid = user != null ? user.getUid() : null;
        return uid != null ? mAccountMap.get(uid) : null;
    }

    /** Deal with authentication backend changes: sign in and sign out */
    @Override public void onAuthStateChanged(@NonNull final FirebaseAuth auth) {
        // Post the change event.
        EventBus.getDefault().post(new AccountStateChangeEvent(getCurrentAccount(auth)));
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
        setValueEventListener(new AccountChangeHandler(mAccountMap));
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    /** Unregister the component during lifecycle pause events. */
    public void unregister() {
        EventBus.getDefault().unregister(this);
        setValueEventListener(null);
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

    /** Set or clear the account change value event listener. */
    private void setValueEventListener(final AccountChangeHandler handler) {
        // Determine whether to add or remove the account value event listener.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String path = String.format("/accounts/%s", uid);
            DatabaseReference database = FirebaseDatabase.getInstance().getReference(path);
            if (handler != null) {
                // Install a handler.
                mAccountChangeHandler = handler;
                database.addValueEventListener(mAccountChangeHandler);
            } else {
                // Remove a previously installed handler.
                database.removeEventListener(mAccountChangeHandler);
            }
        }
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

    /** Provide a class to handle account changes. */
    private class AccountChangeHandler implements ValueEventListener {

        // Private instance constants.

        /** The logcat TAG. */
        private final String TAG = this.getClass().getSimpleName();

        // Private instance variables.

        /** The top level view affected by change events. */
        private Map<String, Account> mAccountMap;

        // Public constructors.

        /** Build a handler with a given top level layout view. */
        AccountChangeHandler(final Map<String, Account> accountMap) {
            mAccountMap = accountMap;
        }

        /** Get the current of account using a list of account identifiers. */
        @Override public void onDataChange(final DataSnapshot dataSnapshot) {
            // Determine how many accounts are available to extract rooms from.
            Log.d(TAG, "Account value is: " + dataSnapshot.getValue());
            Account account = dataSnapshot.getValue(Account.class);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user != null ? user.getUid() : null;
            if (uid != null) mAccountMap.put(uid, account);
        }

        /** ... */
        @Override public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }

        // Private instance methods.

    }

}
