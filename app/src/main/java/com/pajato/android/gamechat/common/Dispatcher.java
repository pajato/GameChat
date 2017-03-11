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

    /** The experience or message key. */
    public String key;

    /** The room key. */
    public String roomKey;

    /** The fragment type denoting the fragment index and the experience type. */
    public FragmentType type;

    // Public Constructors.

    /** Build an instance given a type. */
    Dispatcher(final FragmentType type) {
        // Capture the type and handle any of the experience types.
        this.type = type;
    }

    /** Build an instance given a list item. */
    Dispatcher(final FragmentType type, final ListItem item) {
        // Determine if either the type or the item is null.  Abort if so, otherwise case on the
        // type to handle the dispatch setup.
        this.type = type;
        if (type == null || item == null)
            return;
        switch (type) {
            case checkers:
            case chess:
            case tictactoe:
                groupKey = item.groupKey;
                roomKey = item.roomKey;
                key = item.key;
                break;
            case messageList:
                groupKey = item.groupKey;
                roomKey = item.roomKey;
                break;
            case roomMembersList:
                groupKey = item.groupKey;
                key = item.roomKey;
                roomKey = item.roomKey;
                break;
            case experienceList:
                groupKey = item.groupKey;
                roomKey = item.roomKey;
                break;
            default:
                groupKey = item.groupKey;
                roomKey = item.key;
                break;
        }
    }

    /** Build an instance providing a fragment type and a target (experience) fragment type. */
    public Dispatcher(final FragmentType type, final ExpType expType) {
        this.type = type;
        this.expType = expType;
        groupKey = AccountManager.instance.getMeGroupKey();
        roomKey = AccountManager.instance.getMeRoomKey();
    }
}
