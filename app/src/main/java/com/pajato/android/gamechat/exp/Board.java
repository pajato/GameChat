/*
 * Copyright (C) 2017 Pajato Technologies, Inc.
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

import android.support.annotation.NonNull;

import com.pajato.android.gamechat.exp.chess.ChessPiece;

import java.util.Map;
import java.util.Set;

/**
 * Provide an interface for which two-player game experience boards are expected to satisfy. Chess
 * and Checkers are the first two games to use (implement) this interface.
 *
 * @author Paul Michael Reilly
 */

public interface Board {

    /** Return the default color for the piece at the given position. */
    int getDefaultColor(int position);

    /** Return the default text value for the piece at the given position. */
    String getDefaultText(int position);

    /** Return a set of position keys in the board model. */
    Set<String> getKeySet();

    /** Return -1 or the board position corresponding to a given key. */
    int getPosition(final String key);

    /** Return null or the unicode text associated with a piece with a given position. */
    int getTypeface(int position);

    /** Setup the piece at the given position for the start of a game. */
    void setDefault(int position);
}
