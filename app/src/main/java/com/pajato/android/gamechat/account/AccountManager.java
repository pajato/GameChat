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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.signin.SignInActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the account related aspects of the GameChat application.  These include setting up the
 * first time sign-in, creating a persona (nickname and avatar), switching accounts and personas,
 * ...
 *
 * @author Paul Michael Reilly
 */
public enum AccountManager implements FirebaseAuth.AuthStateListener {
    instance;

    public static enum Actions {signIn, signOut}

    // Private class constants

    /** The logcat logger tag. */
    private static final String TAG = AccountManager.class.getSimpleName();

    // Private instance variables

    /** The account repository associating mulitple account id strings with the cloud account. */
    private ConcurrentHashMap<String, Account> mAccountMap = new ConcurrentHashMap<>();

    /** The currently active account ID. */
    private String mActiveAccountId;

    /** The array of click keys.  The ... */
    private SparseArray<Actions> mActionMap = new SparseArray<>();

    // Public instance methods

    /** Deal with authentication changes like sign in and sign out */
    @Override public void onAuthStateChanged(@NonNull final FirebaseAuth auth) {
        FirebaseUser user = auth.getCurrentUser();
        Account account = null;
        if (user != null) {
            // User is signed in.  Add this account.
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            account = getAccount(user);
            mActiveAccountId = account.getAccountId();
            mAccountMap.put(mActiveAccountId, account);
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
        EventBus.getDefault().post(new AccountStateChangeEvent(account));
    }

    /** Return null if there is no active account, the current active account otherwise. */
    public Account getActiveAccount() {
        return mActiveAccountId != null ? mAccountMap.get(mActiveAccountId) : null;
    }
    /** Return true iff there is an account to select from. */
    public boolean hasAccount() {
        // The account is considered missing if there is no value with an associated key in the map.
        return mAccountMap.size() > 0;
    }

    /** Initialize the account manager. */
    public void init(final AppCompatActivity context) {
        // Build a sparse array of click key values.
        mActionMap.put(R.id.signIn, Actions.signIn);
        mActionMap.put(R.id.signOut, Actions.signOut);
    }

    /** Register the component during lifecycle resume events. */
    public void register() {
        EventBus.getDefault().register(this);
        // Deal with auth events in the listener.
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    /** Register a given account and provider ids. */
    public void register(final String accountId) {
        // Perform the Firebase authentication thing using the given ids.
    }

    /** Unregister the component during lifecycle pause events. */
    public void unregister() {
        EventBus.getDefault().unregister(this);
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    /** Handle a sign in or sign out operation. */
    @Subscribe public void processClick(final ClickEvent event) {
        // Case on the view's tag content.
        Actions action = mActionMap.get(event.getValue());
        if (action != null) {
            switch (action) {
                case signIn:
                    // Invoke the sign in process.
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
    }

    // Private instance methods.

    /** Map the Firebase User to an Account. */
    private Account getAccount(final FirebaseUser user) {
        Account result = new Account();
        result.setAccountId(user.getEmail());
        result.setAccountUrl(user.getPhotoUrl());
        result.setDisplayName(user.getDisplayName());
        result.getAvatarMap().put(user.getDisplayName(), user.getPhotoUrl());
        result.setProviderId(user.getProviderId());
        return result;
    }

    // Private classes

}
