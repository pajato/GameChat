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

    /**
     * The logcat tag.
     */
    private static final String TAG = BaseGameFragment.class.getSimpleName();

    /**
     * The lifecycle event format string with no bundle.
     */
    private static final String FORMAT_NO_BUNDLE =
            "Event: %s; Fragment: %s; Fragment Manager: %s.";

    /**
     * The lifecycle event format string with a bundle provided.
     */
    private static final String FORMAT_WITH_BUNDLE =
            "Event: %s; Fragment: %s; Fragment Manager: %s; Bundle: %s.";

    // Package private instance variables.

    /**
     * The game type (enum), set at fragment view creation time.
     */
    GameManager.Game mGame;

    /**
     * The current turn indicator: True = Player 1, False = Player 2.
     */
    boolean mTurn;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseGameFragment() {}

    // Public instance methods.

    /** Return the current turn indicator. */
    public boolean getTurn() {
        return mTurn;
    }

    /**
     * not sure yet what this is all about.
     */
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

}
