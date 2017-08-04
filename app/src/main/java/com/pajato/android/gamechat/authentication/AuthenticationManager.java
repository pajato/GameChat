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

package com.pajato.android.gamechat.authentication;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthStateChangedEvent;
import com.pajato.android.gamechat.event.RegistrationChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.event.RegistrationChangeEvent.REGISTERED;
import static com.pajato.android.gamechat.main.MainActivity.RC_SIGN_IN;

/**
 * Manages the Firebase sign-in and sign-out related aspects of the GameChat application.  These
 * include setting up the first time sign-in result, signing out on this device and switching
 * accounts.
 *
 * @author Paul Michael Reilly on 8/04/2017 (refactored from AccountManager)
 */
public enum AuthenticationManager implements FirebaseAuth.AuthStateListener {
    instance;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = AuthenticationManager.class.getSimpleName();

    // Private instance variables

    /** A flag indicating that Firebase is enabled (registered) or not. */
    private boolean mIsFirebaseEnabled = false;

    /** A map tracking registrations from the key classed that are needed to enable Firebase. */
    private Map<String, Boolean> mRegistrationClassNameMap = new HashMap<>();

    // Public class methods.

    /** Return an intent object suitable to sign-in a Firebase User. */
    public static Intent getAuthIntent() {
        // Get an intent for which to handle the authentication mode.  While in development mode,
        // disable smart lock.
        AuthUI.SignInIntentBuilder intentBuilder = AuthUI.getInstance().createSignInIntentBuilder();
        intentBuilder.setProviders(Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()));
        intentBuilder.setLogo(R.drawable.signin_logo);
        intentBuilder.setTheme(R.style.signInTheme);
        //intentBuilder.setIsSmartLockEnabled(!BuildConfig.DEBUG);
        Intent intent = intentBuilder.build();
        intent.putExtra("signin", true);
        return intent;
    }

    /** Handle a sign in click event by starting a Firebase authentication activity. */
    public static void signIn(final Activity activity) {
        // Start the Firebase sign-in using the standard app sign-in intent.
        activity.startActivityForResult(getAuthIntent(), RC_SIGN_IN);
    }

    /** Perform a signout or switch to the User with the given (non-null) email address. */
    public static void signOut(final FragmentActivity activity, final String email) {
        AuthUI.getInstance()
                .signOut(activity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // The User is now signed out. Determine if another User should be signed in.
                        Log.d(TAG, "Log out is complete.");
                        if (email != null)
                            signIn(email);
                    }
                });
    }

    // Public instance methods

    /** Initialize the account manager with the given set of localized messages and class names. */
    public void init(@NonNull final List<String> list) {
        // Initialize the account manager by setting the list of localized messages that may be
        // used subsequently and the list of class names all of which must be registered in order
        // for Firebase to be enabled.
        for (String name : list)
            mRegistrationClassNameMap.put(name, false);
    }

    /** Deal with Firebase authentication backend changes: sign in and sign out */
    @Override public void onAuthStateChanged(@NonNull final FirebaseAuth auth) {
        // Determine if this state change represents a User signing in.
        FirebaseUser user = auth.getCurrentUser();
        AppEventManager.instance.post(new AuthStateChangedEvent(user));
        if (user != null)
            Log.i(TAG, "Authentication sign-in for: " + user.getEmail());
    }

    /** Handle a registration event by enabling and/or disabling Firebase, as necessary. */
    @Subscribe public void onRegistrationChange(final RegistrationChangeEvent event) {
        // Filter out registration events on class names the account manager does not care about.
        if (!mRegistrationClassNameMap.containsKey(event.name))
            return;

        // The event is of interest. Update the map and determine if Firebase needs to be enabled or
        // disabled.  When all of the values in the registration class name map are true, the app is
        // ready for the account manager to be registered.  When any of the values are false, the
        // account manager will be unregistered.
        mRegistrationClassNameMap.put(event.name, event.changeType == REGISTERED);
        for (Boolean value : mRegistrationClassNameMap.values())
            if (!value) {
                setFirebaseState(false);
                return;
            }

        // If the account manager is not currently listening for account changes, do so now.
        if (!mIsFirebaseEnabled) {
            setFirebaseState(true);
        }
    }

    // Private class methods.

    /** Handle a switch user event by attempting a sign-in to the given email address. */
    private static void signIn(@NonNull final String email) {
        // Ensure that the given email address is valid.  Abort if not.
        AuthCredential authCredential = CredentialsManager.instance.getAuthCredential(email);
        if (authCredential == null)
            return;

        // Perform the sign-in.
        FirebaseAuth.getInstance().signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Exception exc = task.getException();
                            String excMessage = exc != null ? exc.getMessage() : "N/A";
                            Log.i(TAG, "Sign operation failed with exception: " + excMessage);
                        }
                    }
                });
    }

    // Private instance methods.

    /** Set the Firebase state per the given value. */
    private void setFirebaseState(final boolean value) {
        // Ensure that no work is done unnecessarily by detecting unchanged state.
        if ((value && mIsFirebaseEnabled) || (!value && !mIsFirebaseEnabled))
            return;

        // Disable Firebase by turning off the authentication change listener.
        String state = value ? "enabled" : "disabled";
        Log.d(TAG, "Firebase is now " + state + ".");
        if (value)
            FirebaseAuth.getInstance().addAuthStateListener(this);
        else {
            // Disable Firebase by removing the auth state change listener and ensure that the
            // current account (if any) is cleared.
            FirebaseAuth.getInstance().removeAuthStateListener(this);
            AppEventManager.instance.post(new AuthStateChangedEvent(null));
        }
        mIsFirebaseEnabled = value;
    }
}
