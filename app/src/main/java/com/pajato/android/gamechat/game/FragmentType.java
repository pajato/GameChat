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

package com.pajato.android.gamechat.game;

/**
 * Defines the fragments that can be shown in the experience (aka "games") panel.
 *
 * @author Paul Michael Reilly
 */
public enum FragmentType {
    signedOut (ShowSignedOutFragment.class),
    noExp (ShowNoGamesFragment.class),
    groupList (ShowExpGroupListFragment.class),
    roomList (ShowExpRoomListFragment.class),
    expList (ShowExpListFragment.class),
    tictactoe (TTTFragment.class, ExpType.ttt),
    checkers (CheckersFragment.class, ExpType.checkers),
    chess (ChessFragment.class, ExpType.chess);

    // Private instance variables.

    /** The fragment base class for the type. */
    public  Class<? extends BaseGameFragment> fragmentClass;

    /** The experience type for this value. */
    public ExpType expType;

    /** Build an instance with only a given fragment class. */
    FragmentType(final Class<? extends BaseGameFragment> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    /** Build an instance with both a given fragment class and an experience type. */
    FragmentType(final Class<? extends BaseGameFragment> fragmentClass, final ExpType expType) {
        this.fragmentClass = fragmentClass;
        this.expType = expType;
    }

}