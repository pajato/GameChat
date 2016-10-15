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

import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.event.AppEventManager;

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

    /** The current turn indicator: True = Player 1, False = Player 2. */
    boolean mTurn;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseGameFragment() {}

    // Public instance methods.

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

    /** Create a new experience to be displayed in this fragment. */
    protected void createExperience(final Context context, final Dispatcher dispatcher) {
        // nop; the subclass should handle this.
    }

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
    protected void setupExperience(final Context context, final Dispatcher dispatcher) {
        // Ensure that the dispatcher is valid.  Abort if not.
        // TODO: might be better to show a toast or snackbar on error.
        if (dispatcher == null || dispatcher.type == null) return;

        // Determine if the fragment type does not require an experience. Abort if not.
        ExpType expType = dispatcher.type.expType;
        if (expType == null) return;

        // Determine if an experience is available via the dispatcher and fetch it.
        mExperience = dispatcher.expKey != null ? getExperience(dispatcher) : null;

        // Determine if an experience should be created.  If so use the passed in context in setting
        // up the experience as the current context for this fragment may not exist yet.
        if (dispatcher.expKey == null) createExperience(context, dispatcher);
    }

    // Private instance methods.

    /** Return the experience using the given dispatcher, null if there is no experience. */
    private Experience getExperience(Dispatcher dispatcher) {
        // Ensure there is a room key to use.  Abort if not.
        String key = dispatcher.expKey;
        if (key == null) return null;

        // There is a key. Use it to get the experience map.
        return DatabaseListManager.instance.experienceMap.get(key);
    }

}
