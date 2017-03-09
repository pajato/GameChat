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

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.exp.checkers.CheckersEngine;
import com.pajato.android.gamechat.exp.chess.ChessEngine;
import com.pajato.android.gamechat.exp.model.Checkers;
import com.pajato.android.gamechat.exp.chess.Chess;
import com.pajato.android.gamechat.exp.model.TicTacToe;

import static com.pajato.android.gamechat.common.FragmentType.checkers;
import static com.pajato.android.gamechat.common.FragmentType.chess;
import static com.pajato.android.gamechat.common.FragmentType.tictactoe;

/**
 * The games enum values associate games, modes, fragments and resources in a very flexible, concise
 * fashion.
 *
 * @author Paul Michael Reilly
 */
public enum ExpType {
    checkersET(Checkers.class, R.mipmap.ic_checkers, R.string.PlayCheckers, R.string.player1, R.string.player2,
            R.string.CheckersDisplayName),
    chessET(Chess.class, R.mipmap.ic_chess, R.string.PlayChess, R.string.player1, R.string.player2,
            R.string.ChessDisplayName),
    tttET(TicTacToe.class, R.mipmap.ic_tictactoe_red, R.string.PlayTicTacToe, R.string.xValue, R.string.oValue,
            R.string.TicTacToeDisplayName);

    // Instance variables.

    /** The concrete experience (model) fragment type. */
    public Class experienceClass;

    /** The primary player index. */
    int mPrimaryIndex;

    /** The secondary player index. */
    int mSecondaryIndex;

    /** The game icon resource id. */
    int mIconResId;

    /** The game title resource id. */
    int mTitleResId;

    /** The display name resource id */
    public int displayNameResId;

    // Constructor.

    /** Build an instance given the online, local and computer opponent fragment indexes. */
    ExpType(final Class expClass, final int iconId, final int titleId, final int primary,
            final int secondary, final int displayNameResId) {
        experienceClass = expClass;
        mIconResId = iconId;
        mTitleResId = titleId;
        mPrimaryIndex = primary;
        mSecondaryIndex = secondary;
        this.displayNameResId = displayNameResId;
    }

    /** Return the fragment type associated with this experience type. */
    public FragmentType getFragmentType() {
        switch (this) {
            case checkersET:
                return checkers;
            case chessET:
                return chess;
            case tttET:
                return tictactoe;
        }
        return null;
    }

    /** Return the fragment type associated with this experience type. */
    public Engine getEngine() {
        switch (this) {
            case checkersET:
                return CheckersEngine.instance;
            case chessET:
                return ChessEngine.instance;
            case tttET:
                return null;
        }
        return null;
    }

}
