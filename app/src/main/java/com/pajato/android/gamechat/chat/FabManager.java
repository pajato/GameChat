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
import android.util.SparseArray;
import android.view.View;

import com.pajato.android.gamechat.R;

import java.util.Locale;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.pajato.android.gamechat.chat.FabManager.State.opened;


/** Provide a singleton to manage the rooms panel fab button. */
public enum FabManager {
    chat(R.id.chatFab, R.id.chatFabMenu),
    game(R.id.games_fab, R.id.games_fab_menu);

    FabManager(final int fabId, final int fabMenuId) {
        mFabId = fabId;
        mFabMenuId = fabMenuId;
    }

    /** Provide FAB state constants. */
    enum State {opened, closed}

    // Private instance variables.

    /** The fab resource identifier. */
    private int mFabId;

    /** The fab menu resource identifier. */
    private int mFabMenuId;

    /** The tag used to find the main chat fragment, which spawns all others. */
    private String mTag;

    /** The association of FAB button resource identifiers and their menu layout. */
    private SparseArray<View> mMenuMap = new SparseArray<>();

    // Public instance methods

    /** Initialize the fab button. */
    public void init(@NonNull final View layout, final String tag) {
        // Initialize the fab button state to opened and then toggle it to put the panel into the
        // correct initial state.
        mTag = tag;
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        View menu = layout.findViewById(mFabMenuId);
        fab.setTag(R.integer.fabStateKey, opened);
        mMenuMap.put(mFabId, menu);
        dismissMenu(layout);
    }

    /** Dismiss the menu associated with the given layout view. */
    public void dismissMenu(@NonNull final View layout) {
        // The fragment is accessible and the layout has been established.  Finish dismissing the
        // fab menu.
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setImageResource(R.drawable.ic_add_white_24dp);
        fab.setTag(R.integer.fabStateKey, State.closed);
        View menu = mMenuMap.get(mFabId);
        menu.setVisibility(View.GONE);
    }

    /** Dismiss the menu associated with the given FAB button. */
    public void dismissMenu(@NonNull final Fragment fragment) {
        // Determine if the chat fragment is accessible.  If not, abort.
        View layout = getView(fragment);
        if (layout != null) dismissMenu(layout);
    }

    /** Return the given fragment's layout view, if one exists, otherwise null. */
    private View getView(@NonNull final Fragment fragment) {
        // Determine if the given fragment has an associated fragment.
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            // The fragment does not have an associated activity!.
            String format = "The fragment {%s} does not have an attached activity!";
            Log.e(TAG, String.format(Locale.US, format, fragment));
            return null;
        }

        // Determine if the attached fragment has a view.  If so return the view, otherwise null.
        Fragment chatFragment = activity.getSupportFragmentManager().findFragmentByTag(mTag);
        return chatFragment != null ? chatFragment.getView() : null;
    }

    /** Set the FAB visibility state. */
    public void setState(@NonNull final Fragment fragment, final int state) {
        View layout = getView(fragment);
        if (layout == null) return;

        // ...
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setVisibility(state);
    }

    /** Toggle the state of the FAB button. */
    public void toggle(@NonNull final Fragment fragment) {
        // Determine if the fab view STATE tag has a valid state value and the content view exists.
        View layout = getView(fragment);
        if (layout == null) return;

        // The layout view is valid.  Use it to toggle the fab state.
        View contentView = fragment.getView();
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        Object payload = fab.getTag(R.integer.fabStateKey);
        if (payload instanceof State) {
            // It does.  Toggle it by casing on the value to show and hide the relevant views.
            State value = (State) payload;
            switch (value) {
                case opened:
                    // The FAB is showing X and menu is visible.  Set the icon to +, close the
                    // menu and undim the frame.
                    dismissMenu(layout);
                    if (contentView != null) contentView.setVisibility(View.VISIBLE);
                    break;
                case closed:
                    // The FAB is showing + and the menu is not visible.  Set the icon to X and open
                    // the menu.
                    fab.setImageResource(R.drawable.ic_clear_white_24dp);
                    fab.setTag(R.integer.fabStateKey, opened);
                    if (contentView != null) contentView.setVisibility(View.GONE);
                    View menu = mMenuMap.get(mFabId);
                    menu.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

}
