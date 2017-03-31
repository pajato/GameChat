/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.common;

import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.exp.ExpType;

/**
 * The fragment dispatcher provides mediation between the experience or chat managers and the main
 * (envelope, experience or chat) fragment. It captures all information a delegated fragment will
 * need to instantiate and take the foreground.
 *
 * @author Paul Michael Reilly
 */
public class Dispatcher {

    // Public instance variables.

    /** The experience target type. */
    public ExpType expType;

    /** The group key. */
    public String groupKey;

    /** The experience key. */
    public String key;

    /** The room key. */
    public String roomKey;

    /** The type of the fragment which is launching a dispatch transition */
    public FragmentType startType;

    /** The type of the originating fragment in a dispatch chain; may be null */
    public FragmentType launchType;

    /** The type of the desired target fragment. */
    public FragmentType type;

    // Public Constructors.

    /**
     * Create a dispatcher object
     * @param fragment the fragment initiating this transition
     * @param type the desired target fragment type
     * @param launchType the type of the fragment which initiated this chain (or null)
     * @param item a ListItem object used to configure the dispatcher (or null)
     */
    Dispatcher(final BaseFragment fragment, final FragmentType type,
               final FragmentType launchType, final ListItem item) {
        this.expType = type.expType;
        if (item != null) {
            this.groupKey = item.groupKey;
            this.roomKey = item.roomKey;
            this.key = item.experienceKey;
        } else if (fragment.mDispatcher != null) {
            this.groupKey = fragment.mDispatcher.groupKey;
            this.roomKey = fragment.mDispatcher.roomKey;
            this.key = fragment.mDispatcher.key;
        }
        this.startType = fragment.type;
        this.type = type;
        this.launchType = launchType;
        if (this.launchType == null && fragment.mDispatcher != null)
            this.launchType = fragment.mDispatcher.launchType;
    }

    /** Build an instance given a target type. */
    Dispatcher(final FragmentType type) {
        this.type = type;
        this.startType = null;
    }

    /**
     * Build an instance providing a start fragment type and a target (experience) fragment type. Only
     * used by DispatchManager.dispatchToGame and always assumes the 'me' group/room.
     */
    Dispatcher(final FragmentType startFragmentType, final FragmentType type, final ExpType expType) {
        this.startType = startFragmentType;
        this.type = type;
        this.expType = expType;
        this.groupKey = AccountManager.instance.getMeGroupKey();
        this.roomKey = AccountManager.instance.getMeRoomKey();
    }
}
