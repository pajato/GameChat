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

package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.exp.model.ExpProfile;

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

    // Protected instance methods.

    /** Create a new experience to be displayed in this fragment. */
    protected void createExperience(final Context context, final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
        // nop; the subclass should handle this.
    }

    /** Return TRUE iff the User has requested to play again. */
    protected boolean isPlayAgain(final Object tag, final String className) {
        // Determine if the given tag is the class name, i.e. a snackbar action request to play
        // again.
        return ((tag instanceof String && className.equals(tag)) ||
                (tag instanceof MenuEntry && ((MenuEntry) tag).titleResId == R.string.PlayAgain));
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

    /** Process the dispatcher to set up the experience fragment. */
    @Override protected boolean onDispatch(@NonNull final Context context,
                                           @NonNull final Dispatcher dispatcher) {
        return true;
    }

    /** Implement the setTitles() contract. */
    @Override protected void setTitles(final String groupKey, final String roomKey) {
        // Ensure that there is an accessible toolbar.  Abort if not, otherwise show the room name
        // as the title and the group name as the subtitle.
        Toolbar bar = mLayout != null ? (Toolbar) mLayout.findViewById(R.id.toolbar) : null;
        if (bar == null) return;
        String title = DatabaseListManager.instance.getRoomName(roomKey);
        String subtitle = DatabaseListManager.instance.getGroupName(groupKey);
        bar.setTitle(title);
        bar.setSubtitle(subtitle);
    }

    /** Provide a default implementation for setting up an experience. */
    protected void setupExperience(final Context context, final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
        // Ensure that the dispatcher is valid.  Abort if not.
        // TODO: might be better to show a toast or snackbar on error.
        if (dispatcher == null || dispatcher.type == null) return;

        // Determine if the fragment type does not require an experience. Abort if not.
        ExpType expType = dispatcher.type.expType;
        if (expType == null) return;

        // Determine if the dispatcher has a single experience profile.
        if (dispatcher.payload != null) {
            // It does.  Either get the cached experience or fetch it from the database.
            Experience exp = DatabaseListManager.instance.experienceMap.get(dispatcher.key);
            if (exp == null) {
                // Fetch the experience from the database.
                DatabaseListManager.instance.setExperienceWatcher(dispatcher.payload);
            }
        } else
            // Create a new experience.
            createExperience(context, dispatcher);
    }
}
