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

import com.pajato.android.gamechat.R;

/**
 * The games enum values associate games, modes, fragments and resources in a very flexible, concise
 * fashion.
 *
 * @author Paul Michael Reilly
 */
enum ExpType {
    checkers (R.mipmap.ic_checkers, R.string.PlayCheckers, R.string.player1, R.string.player2),
    chess (R.mipmap.ic_chess, R.string.PlayChess, R.string.player1, R.string.player2),
    ttt (R.mipmap.ic_tictactoe_red, R.string.PlayTicTacToe, R.string.xValue, R.string.oValue);

    // Instance variables.

    /** The primary player index. */
    int primaryIndex;

    /** The secondary player index. */
    int secondaryIndex;

    /** The game icon resource id. */
    int iconResId;

    /** The game title resource id. */
    int titleResId;

    // Constructor.

    /** Build an instance given the online, local and computer opponent fragment indexes. */
    ExpType(final int iconId, final int titleId, final int primary, final int secondary) {
        iconResId = iconId;
        titleResId = titleId;
        primaryIndex = primary;
        secondaryIndex = secondary;
    }
}