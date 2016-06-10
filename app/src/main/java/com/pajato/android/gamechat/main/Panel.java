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

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.fragment.ChatFragment;
import com.pajato.android.gamechat.fragment.GameFragment;
import com.pajato.android.gamechat.fragment.MembersFragment;
import com.pajato.android.gamechat.fragment.RoomsFragment;

/**
 * Provide an enumeration of panels used in the app.
 */
public enum Panel {
    chat(R.string.chat, R.layout.fragment_chat, ChatFragment.class),
    game(R.string.game, R.layout.fragment_game, GameFragment.class),
    members(R.string.members, R.layout.fragment_members, MembersFragment.class),
    rooms(R.string.rooms, R.layout.fragment_rooms, RoomsFragment.class);

    /** The logcat tag. */
    private static final String TAG = Panel.class.getSimpleName();

    /** The panel title resource id. */
    private int titleId;

    /** The fragment associated with the panel. */
    private Fragment fragment;

    /** The fragment class. */
    private Class<? extends Fragment> fragmentClass;

    /** The panel layout id. */
    private int layoutId;

    /**
     * Create the enum value instance given a title resource id, layout resource id and fragment class..
     *
     * @param titleId The given title id.
     * @param layoutId The given layout id.
     * @param fragmentClass The given layout class.
     */
    Panel(final int titleId, final int layoutId, final Class<? extends Fragment> fragmentClass) {
        this.titleId = titleId;
        this.layoutId = layoutId;
        this.fragmentClass = fragmentClass;
    }

    /** @return The panel title string. */
    public int getTitleId() {
        return titleId;
    }

    /**
     * Builds a fragment associated with the panel using lazy creation, i.e. defer instantiation until the fragment
     * is actually needed.
     *
     * @return The panel fragment.
     */
    public Fragment getFragment(final Context context) {
        if (fragment == null) createFragment(context);
        return fragment;
    }

    /**
     * Create a panel fragment using the title resource id as a discriminant.
     *
     * @return The newly created fragment or null if the fragment cannot be created.
     */
    private void createFragment(final Context context) {
        try {
            String name = fragmentClass.getName();
            switch (titleId) {
                case R.string.chat:
                    fragment = ChatFragment.instantiate(context, name);
                    break;
                case R.string.game:
                    fragment = GameFragment.instantiate(context, name);
                    break;
                case R.string.members:
                    fragment = MembersFragment.instantiate(context, name);
                    break;
                case R.string.rooms:
                    fragment = RoomsFragment.instantiate(context, name);
                    break;
            }
        } catch (Fragment.InstantiationException exc) {
            String format = "Could not create the fragment for the {%s} panel.";
            Log.e(TAG, String.format(format, context.getString(titleId)), exc);
        }
    }
}
