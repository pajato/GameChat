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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.database.DatabaseEventHandler;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.signin.SignInActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.account.Account.STANDARD;

/**
 * Manages the account related aspects of the GameChat application.  These include setting up the
 * first time sign-in result, creating and persisting a profile on this device and switching
 * accounts.
 *
 * @author Paul Michael Reilly
 */
public enum AccountManager implements FirebaseAuth.AuthStateListener {
    instance;

    // Public enum constants.

    /** The account login states. */
    public enum Actions {signIn, signOut}

    /** A key used to access account available data. */
    public static final String ACCOUNT_AVAILABLE_KEY = "accountAvailable";

    // Private class constants

    // Private instance variables

    /** The current account key, null if there is no current account. */
    private String mCurrentAccountKey;

    /** The account repository associating mulitple account id strings with the cloud account. */
    private Map<String, Account> mAccountMap = new HashMap<>();

    /** The array of click keys.  The ... */
    private SparseArray<Actions> mActionMap = new SparseArray<>();

    // Public instance methods

    /** Retrun the account for the current User, null if there is no signed in User. */
    public Account getCurrentAccount() {
        return mCurrentAccountKey == null ? null : mAccountMap.get(mCurrentAccountKey);
    }

    /** Retrun the account for the current User, null if there is no signed in User. */
    public Account getCurrentAccount(final Context context) {
        // Determine if there is a logged in account.  If so, return it.
        if (mCurrentAccountKey != null) return mAccountMap.get(mCurrentAccountKey);

        // The User is not signed in.  Prompt them to do so now.
        String text = "Not logged in!  Please sign in.";
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        return null;
    }

    /** Return the current account id, null if there is no curent signed in User. */
    public String getCurrentAccountId() {
        return mCurrentAccountKey;
    }

    /** Return a joined room entry, well formed (space separated) group key and room key pair. */
    public String getJoinedRoomEntry(final String groupKey, final String roomKey) {
        return String.format(Locale.US, "%s %s", groupKey, roomKey);
    }

    /** Obtain a suitable Uri to use for the User's icon. */
    public String getPhotoUrl(FirebaseUser user) {
        // TODO: figure out how to handle a generated icon ala Inbox, Gmail and Hangouts.
        Uri icon = user.getPhotoUrl();
        return icon != null ? icon.toString() : null;
    }

    /** Deal with authentication backend changes: sign in and sign out */
    @Override public void onAuthStateChanged(@NonNull final FirebaseAuth auth) {
        // Determine if this state represents a User signing in or signing out.
        String name = "accountStateChangeHandler";
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // A User has signed in. Set up a database listener for the associated account.  That
            // listener will post an account change event with the account information to the app.
            String path = String.format("/accounts/%s", user.getUid());
            DatabaseEventHandler handler = DatabaseManager.instance.getHandler(name);
            if (handler == null) handler = new AccountChangeHandler(name, path);
            DatabaseManager.instance.registerHandler(handler);
        } else {
            // The User is signed out.  Clear the current account key and notify the app of the sign
            // out event.
            mCurrentAccountKey = null;
            if (DatabaseManager.instance.isRegistered(name)) {
                DatabaseManager.instance.unregisterHandler(name);
                EventBus.getDefault().post(new AccountStateChangeEvent(null));
            }
        }
    }

    /** Initialize the account manager. */
    public void init() {
        // Build a sparse array associating auth events by resource id (User clicked in a sign in or
        // sign out button) with an auth action, register the app event bus and finally add teh
        // database auth change handler.  The auth state change listener will post app events so
        // must come last.
        mActionMap.put(R.id.signIn, Actions.signIn);
        mActionMap.put(R.id.signOut, Actions.signOut);
    }

    /** Handle a sign in or sign out button click. */
    @Subscribe public void processClick(final ClickEvent event) {
        // Case on the view's tag content.
        Actions action = mActionMap.get(event.getValue());
        if (action != null) processAction(event, action);
    }

    /** Register the account manager with the database and app event bus listeners. */
    public void register() {
        EventBusManager.instance.register(this);
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    /** Unregister the component during lifecycle pause events. */
    public void unregister() {
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        EventBusManager.instance.unregister(this);
    }

    // Private instance methods.

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
    private class AccountChangeHandler extends DatabaseEventHandler implements ValueEventListener {

        // Private instance constants.

        /** The logcat TAG. */
        private final String TAG = this.getClass().getSimpleName();

        // Public constructors.

        /** Build a handler with a given top level layout view. */
        AccountChangeHandler(final String name, final String path) {
            super(name, path);
        }

        /** Get the current account using a list of account identifiers. */
        @Override public void onDataChange(final DataSnapshot dataSnapshot) {
            // Determine if the account exists.
            Account account = null;
            if (dataSnapshot.exists()) {
                // It does.  Register it and notify the app that this is the new account of record.
                account = dataSnapshot.getValue(Account.class);
                mAccountMap.put(account.id, account);
                mCurrentAccountKey = account.id;
            } else {
                // The account does not exist.  Create it now, ensuring there really is a User.
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) DatabaseManager.instance.createAccount(user, STANDARD);
            }
            EventBus.getDefault().post(new AccountStateChangeEvent(account));
        }

        /** ... */
        @Override public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }

        // Private instance methods.
    }

}
