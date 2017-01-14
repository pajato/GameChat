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

package com.pajato.android.gamechat.exp;

import com.pajato.android.gamechat.exp.fragment.CheckersFragment;
import com.pajato.android.gamechat.exp.fragment.ChessFragment;
import com.pajato.android.gamechat.exp.fragment.ShowExpGroupListFragment;
import com.pajato.android.gamechat.exp.fragment.ShowExpListFragment;
import com.pajato.android.gamechat.exp.fragment.ShowExpRoomListFragment;
import com.pajato.android.gamechat.exp.fragment.ShowExpTypeListFragment;
import com.pajato.android.gamechat.exp.fragment.ShowNoExperiencesFragment;
import com.pajato.android.gamechat.exp.fragment.ShowOfflineFragment;
import com.pajato.android.gamechat.exp.fragment.ShowSignedOutFragment;
import com.pajato.android.gamechat.exp.fragment.TTTFragment;

/**
 * Defines the fragments that can be shown in the experience (aka "games") panel.
 *
 * @author Paul Michael Reilly
 */
public enum ExpFragmentType {
    signedOut (ShowSignedOutFragment.class),
    offline (ShowOfflineFragment.class),
    noExp (ShowNoExperiencesFragment.class),
    groupList (ShowExpGroupListFragment.class),
    roomList (ShowExpRoomListFragment.class),
    expList (ShowExpListFragment.class),
    tictactoeList (ShowExpTypeListFragment.class),
    tictactoe (TTTFragment.class, ExpType.ttt, tictactoeList),
    checkersList (ShowExpTypeListFragment.class),
    checkers (CheckersFragment.class, ExpType.checkers, checkersList),
    chessList(ShowExpTypeListFragment.class),
    chess (ChessFragment.class, ExpType.chess, chessList);

    // Private instance variables.

    /** The fragment base class for the type. */
    public  Class<? extends BaseExperienceFragment> fragmentClass;

    /** The experience type for this value. */
    public ExpType expType;

    /** The show list fragment type for a particular game. */
    public ExpFragmentType showType;

    /** Build an instance with only a given fragment class. */
    ExpFragmentType(final Class<? extends BaseExperienceFragment> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    /** Build an instance with both a given fragment class and an experience type. */
    ExpFragmentType(final Class<? extends BaseExperienceFragment> fragmentClass, final ExpType expType,
                    final ExpFragmentType showType) {
        this.fragmentClass = fragmentClass;
        this.expType = expType;
        this.showType = showType;
    }

}
