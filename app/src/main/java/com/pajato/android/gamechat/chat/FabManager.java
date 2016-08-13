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

import android.support.design.widget.FloatingActionButton;
import android.util.SparseArray;
import android.view.View;

import com.pajato.android.gamechat.R;

import static com.pajato.android.gamechat.chat.FabManager.State.opened;


/** Provide a singleton to manage the rooms panel fab button. */
enum FabManager {
    instance;

    /** Provide FAB state constants. */
    enum State {opened, closed}

    // Private class constants.

    // Private instance variables.

    /** The association of FAB button resource identifiers and their menu layout. */
    private SparseArray<View> mMenuMap = new SparseArray<>();

    /** The assoication of FAB button resource id and the content layout. */
    private SparseArray<View> mContentMap = new SparseArray<>();

    // Public instance methods

    /** Initialize the fab button. */
    public void init(final FloatingActionButton fab, final View content, final View menu) {
        // Initialize the fab button state to opened and then toggle it to put the panel into the
        // correct initial state.
        fab.setTag(R.integer.fabStateKey, opened);
        mContentMap.put(fab.getId(), content);
        mMenuMap.put(fab.getId(), menu);
        toggle(fab);
    }

    /** Toggle the state of the FAB button. */
    public void toggle(final FloatingActionButton fab) {
        // Determine if the fab view STATE tag has a valid state value.
        Object payload = fab.getTag(R.integer.fabStateKey);
        if (payload instanceof State) {
            // It does.  Toggle it by casing on the value to show and hide the relevant views.
            View menu = mMenuMap.get(fab.getId());
            View content = mContentMap.get(fab.getId());
            State value = (State) payload;
            switch (value) {
                case opened:
                    // The FAB is showing X and menu is visible.  Set the icon to +, close the
                    // menu and undim the frame.
                    fab.setImageResource(R.drawable.ic_add_white_24dp);
                    fab.setTag(R.integer.fabStateKey, State.closed);
                    menu.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                    break;
                case closed:
                    // The FAB is showing + and the menu is not visible.  Set the icon to X and open
                    // the menu.
                    fab.setImageResource(R.drawable.ic_clear_white_24dp);
                    fab.setTag(R.integer.fabStateKey, opened);
                    menu.setVisibility(View.VISIBLE);
                    content.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

}
