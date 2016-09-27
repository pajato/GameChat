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

import static com.pajato.android.gamechat.game.GameManager.CHECKERS_INDEX;
import static com.pajato.android.gamechat.game.GameManager.CHESS_INDEX;
import static com.pajato.android.gamechat.game.GameManager.TTT_INDEX;

/**
 * The games enum values associate games, modes, fragments and resources in a very flexible, concise
 * fashion.
 *
 * @author Paul Michael Reilly
 */
enum Game {
    checkers (CHECKERS_INDEX, R.mipmap.ic_checkers, R.string.PlayCheckers, R.string.player_primary,
              R.string.player_secondary, R.string.FutureCheckers),
    chess (CHESS_INDEX, R.mipmap.ic_chess, R.string.PlayChess, R.string.player_primary,
           R.string.player_secondary, R.string.FutureChess),
    ttt (TTT_INDEX, R.mipmap.ic_tictactoe_red, R.string.PlayTicTacToe, R.string.xValue,
         R.string.oValue, R.string.FutureTTT);

    // Instance variables.

    /** The local fragment index. */
    int fragmentIndex;

    /** The primary player index. */
    int primaryIndex;

    /** The secondary player index. */
    int secondaryIndex;

    /** The game icon resource id. */
    int iconResId;

    /** The game title resource id. */
    int titleResId;

    /** The game future feature prefix resource id. */
    int futureResId;

    // Constructor.

    /** Build an instance given the online, local and computer opponent fragment indexes. */
    Game(final int index, final int iconId, final int titleId, final int primary,
         final int secondary, final int futureId) {
        fragmentIndex = index;
        iconResId = iconId;
        titleResId = titleId;
        primaryIndex = primary;
        secondaryIndex = secondary;
        futureResId = futureId;
    }
}
