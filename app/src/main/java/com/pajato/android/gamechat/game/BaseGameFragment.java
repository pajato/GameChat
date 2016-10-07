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

package com.pajato.android.gamechat.game;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.AppEventManager;

import java.util.List;
import java.util.Locale;

/**
 * Provide a base class to support fragment lifecycle debugging.  All fragment lifecycle events are
 * handled by providing logcat tracing information.
 *
 * @author Paul Michael Reilly
 */
public abstract class BaseGameFragment extends BaseFragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseGameFragment.class.getSimpleName();

    /** The lifecycle event format string with no bundle. */
    private static final String FORMAT_NO_BUNDLE =
            "Event: %s; Fragment: %s; Fragment Manager: %s.";

    /** The lifecycle event format string with a bundle provided. */
    private static final String FORMAT_WITH_BUNDLE =
            "Event: %s; Fragment: %s; Fragment Manager: %s; Bundle: %s.";

    // Package private instance variables.


    /** The experience being enjoyed. */
    Experience mExperience;

    /** The fragment type. */
    FragmentType mFragmentType;

    /** The current turn indicator: True = Player 1, False = Player 2. */
    boolean mTurn;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseGameFragment() {}

    // Public instance methods.

    /** Provide a default experience via the subclass providing a list of player accounts. */
    public Experience getDefaultExperience(final List<Account> players) {
        // TODO: By default, log that the subclass does not provide a default experience, generate a
        // Toast or Snackbar for the User and let the User bail via default controls.
        return null;
    }

    /** Return the current turn indicator. */
    public boolean getTurn() {
        return mTurn;
    }

    /** Remove this after dealing with the chess and checkers fragments. */
    abstract public void messageHandler(final String message);

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        logEvent("onAttach");
    }

    /** Log the lifecycle event, stop showing ads and turn off the app event bus. */
    @Override public void onPause() {
        // Log the event and stop listening for app events.
        super.onPause();
        AppEventManager.instance.unregister(this);
    }

    /** Log the lifecycle event and resume showing ads. */
    @Override public void onResume() {
        // Log the event and start listening for app events.
        super.onResume();
        AppEventManager.instance.register(this);
    }

    // Protected instance methods.

    /** Log a lifecycle event that has no bundle. */
    @Override protected void logEvent(final String event) {
        String manager = getFragmentManager().toString();
        String format = FORMAT_NO_BUNDLE;
        Log.v(TAG, String.format(Locale.US, format, event, this, manager));
    }

    /** Log a lifecycle event that has a bundle. */
    @Override protected void logEvent(final String event, final Bundle bundle) {
        String manager = getFragmentManager().toString();
        String format = FORMAT_WITH_BUNDLE;
        Log.v(TAG, String.format(Locale.US, format, event, this, manager, bundle));
    }

    /** Provide a default implementation for setting up an experience. */
    protected void setupExperience(final Dispatcher dispatcher) {
        // Ensure that the dispatcher is valid.  Abort if not.
        // TODO: might be better to show a toast or snackbar on error.
        mExperience = null;
        if (dispatcher == null) return;

        // Use the dispatcher data to set up the default experience.
        mExperience = DatabaseManager.instance.getExperience(this, dispatcher);
    }

}
