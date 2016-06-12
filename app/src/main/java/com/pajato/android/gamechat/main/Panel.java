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

package com.pajato.android.gamechat.main;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.fragment.ChatFragment;
import com.pajato.android.gamechat.fragment.MembersFragment;
import com.pajato.android.gamechat.fragment.RoomsFragment;
import com.pajato.android.gamechat.fragment.TTTFragment;

/**
 * Provide an enumeration of panels used in the app.
 */
public enum Panel {
    chat(R.string.chat, ChatFragment.class),
    ttt(R.string.ttt, TTTFragment.class),
    members(R.string.members, MembersFragment.class),
    rooms(R.string.rooms, RoomsFragment.class);

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = Panel.class.getSimpleName();

    // Private instance variables.

    /** The panel title resource id. */
    private int titleId;

    /** The fragment class associated with the panel. */
    private Class<? extends Fragment> fragmentClass;

    // Constructor.

    /** Create the enum value instance given a title resource id and a fragment class. */
    Panel(final int titleId, final Class<? extends Fragment> fragmentClass) {
        this.titleId = titleId;
        this.fragmentClass = fragmentClass;
    }

    // Public instance methods.

    /** @return The panel title string. */
    public int getTitleId() {
        return titleId;
    }

    /** @return The panel fragment. */
    public Fragment getFragment() {
        Fragment result = null;
        try {
            String format = "getFragment: Creating fragment using class {%s}.";
            Log.d(TAG, String.format(format, fragmentClass));
            result = fragmentClass.newInstance();
            Log.d(TAG, String.format("getFragment: Created fragment {%s}.", result));
        } catch (InstantiationException | IllegalAccessException exc) {
            Log.e(TAG, "Totally unexpected exception creating fragment!", exc);
        }

        return result;
    }
}
