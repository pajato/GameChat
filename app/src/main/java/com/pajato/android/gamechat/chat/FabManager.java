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
import android.util.SparseArray;
import android.view.View;

import com.pajato.android.gamechat.R;

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
    int mFabId;

    /** The fab menu resource identifier. */
    int mFabMenuId;

    /** The fab view. */
    private FloatingActionButton mFab;

    // Private instance variables.

    /** The association of FAB button resource identifiers and their menu layout. */
    private SparseArray<View> mMenuMap = new SparseArray<>();

    // Public instance methods

    /** Initialize the fab button. */
    public void init(@NonNull final View layout) {
        // Initialize the fab button state to opened and then toggle it to put the panel into the
        // correct initial state.
        mFab = (FloatingActionButton) layout.findViewById(mFabId);
        View menu = layout.findViewById(mFabMenuId);
        mFab.setTag(R.integer.fabStateKey, opened);
        mMenuMap.put(mFab.getId(), menu);
        dismissMenu();
    }

    /** Dismiss the menu associated with the given FAB button. */
    public void dismissMenu() {
        mFab.setImageResource(R.drawable.ic_add_white_24dp);
        mFab.setTag(R.integer.fabStateKey, State.closed);
        View menu = mMenuMap.get(mFab.getId());
        menu.setVisibility(View.GONE);
    }

    /** Set the FAB visibility state. */
    public void setState(final int state) {
        mFab.setVisibility(state);
    }

    /** Toggle the state of the FAB button. */
    public void toggle(final View contentView) {
        // Determine if the fab view STATE tag has a valid state value and the content view exists.
        Object payload = mFab.getTag(R.integer.fabStateKey);
        if (payload instanceof State) {
            // It does.  Toggle it by casing on the value to show and hide the relevant views.
            State value = (State) payload;
            switch (value) {
                case opened:
                    // The FAB is showing X and menu is visible.  Set the icon to +, close the
                    // menu and undim the frame.
                    dismissMenu();
                    if (contentView != null) contentView.setVisibility(View.VISIBLE);
                    break;
                case closed:
                    // The FAB is showing + and the menu is not visible.  Set the icon to X and open
                    // the menu.
                    mFab.setImageResource(R.drawable.ic_clear_white_24dp);
                    mFab.setTag(R.integer.fabStateKey, opened);
                    if (contentView != null) contentView.setVisibility(View.GONE);
                    View menu = mMenuMap.get(mFab.getId());
                    menu.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

}
