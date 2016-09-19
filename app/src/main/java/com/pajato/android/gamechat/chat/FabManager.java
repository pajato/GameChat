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

package com.pajato.android.gamechat.chat;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.pajato.android.gamechat.R;

import java.util.Locale;

import static com.pajato.android.gamechat.chat.FabManager.State.opened;


/** Provide a singleton to manage the rooms panel fab button. */
public enum FabManager {
    chat(R.id.chatFab, R.id.chatFabMenu, R.id.chatDimmer),
    game(R.id.games_fab, R.id.games_fab_menu, R.id.gameDimmer);

    /** Provide FAB state constants. */
    enum State {opened, closed}

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = FabManager.class.getSimpleName();

    // Private instance variables.

    /** The fab resource identifier. */
    private int mFabId;

    /** The fab menu resource identifier. */
    private int mFabMenuId;

    /** The resource id used to access the dimmer view used to blur the content. */
    private int mFabDimmerId;

    /** The resource id used to access the main fragment. */
    private String mTag;

    // Sole Constructor.

    /** Build the instance with the given resource ids. */
    FabManager(final int fabId, final int fabMenuId, final int fabDimmerId) {
        mFabId = fabId;
        mFabMenuId = fabMenuId;
        mFabDimmerId = fabDimmerId;
    }

    // Public instance methods

    /** Initialize the fab button. */
    public void init(@NonNull final View layout, final String fragmentTag) {
        // Initialize the fab button state to opened and then toggle it to put the panel into the
        // correct initial state.
        mTag = fragmentTag;
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
    }

    /** Initialize the fab state. */
    public void init(final Fragment fragment) {
        View layout = getView(fragment);
        if (layout == null) return;

        // Set the fab state to closed.
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setTag(R.integer.fabStateKey, opened);
        dismissMenu(layout);
    }

    /** Dismiss the menu associated with the given FAB button. */
    public void dismissMenu(@NonNull final Fragment fragment) {
        // Determine if the chat fragment is accessible.  If not, abort.
        View layout = getView(fragment);
        if (layout != null) dismissMenu(layout);
    }

    /** Set the FAB visibility state. */
    public void setState(@NonNull final Fragment fragment, final int state) {
        // Obtain the layout view owning the FAB.  If it is not accessible, just return since an
        // error message will have been generated.  If it is accessible, apply the given visibility
        // state.
        View layout = getView(fragment);
        if (layout == null) return;
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setVisibility(state);
    }

    /** Toggle the state of the FAB button using a given fragment to obtain the layout view. */
    public void toggle(@NonNull final Fragment fragment) {
        // Determine if the fragment layout exists.  Continue if it does.  Return if it does not.
        // An error message with stack trace will have been generated if the view cannot be
        // accessed.
        View layout = getView(fragment);
        if (layout == null) return;

        // The layout view is valid.  Use it to toggle the fab state.
        View dimmerView = layout.findViewById(mFabDimmerId);
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        Object payload = fab.getTag(R.integer.fabStateKey);
        if (payload instanceof State) {
            // It does.  Toggle it by casing on the value to show and hide the relevant views.
            State value = (State) payload;
            switch (value) {
                case opened:
                    // The FAB is showing 'X' and it's menu is visible.  Set the icon to '+', close
                    // the menu and undim the frame.
                    dismissMenu(layout);
                    dimmerView.setVisibility(View.GONE);
                    break;
                case closed:
                    // The FAB is showing '+' and the menu is not visible.  Set the icon to X and
                    // open the menu.
                    fab.setImageResource(R.drawable.ic_clear_white_24dp);
                    fab.setTag(R.integer.fabStateKey, opened);
                    dimmerView.setVisibility(View.VISIBLE);
                    View menu = layout.findViewById(mFabMenuId);
                    menu.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    // Private instance methods.

    /** Dismiss the menu associated with the given layout view. */
    private void dismissMenu(@NonNull final View layout) {
        // The fragment is accessible and the layout has been established.  Finish dismissing the
        // fab menu.
        View dimmerView = layout.findViewById(mFabDimmerId);
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setImageResource(R.drawable.ic_add_white_24dp);
        fab.setTag(R.integer.fabStateKey, State.closed);
        View menu = layout.findViewById(mFabMenuId);
        menu.setVisibility(View.GONE);
        dimmerView.setVisibility(View.GONE);
    }

    /** Return the given fragment's layout view, if one exists, otherwise null. */
    private View getView(@NonNull final Fragment fragment) {
        // Determine if the given fragment has an associated activity.
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            // The activity is not accessible.  This is generally caused by a software error, in this
            // case, it is likely that an app event subscribed handler has invoked this call
            // inappropriately.  To that end, a stack trace is being appended to the error message
            // being logged.
            Throwable t = new Throwable();
            String format = "The fragment {%s} does not have an activity attached!";
            Log.e(TAG, String.format(Locale.US, format, fragment), t);
            return null;
        }

        // Determine if the attached fragment has a view.  If so return the view, otherwise null.
        Fragment envelopeFragment = activity.getSupportFragmentManager().findFragmentByTag(mTag);
        if (envelopeFragment == null || envelopeFragment.getView() == null) {
            // The envelope fragment or it's layout view is not accessible.  This is generally
            // caused by a software error, in this case, it is likely that an app event subscribed
            // handler has invoked this call inappropriately.  To that end, a stack trace is being
            // appended to the error message being logged.
            Throwable t = new Throwable();
            String format = "The envelope fragment {%s} does not have a layout view!";
            Log.e(TAG, String.format(Locale.US, format, envelopeFragment), t);
            return null;
        }

        // There is a layout view to return.
        return envelopeFragment.getView();
    }

}
