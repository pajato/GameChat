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

import java.util.List;
import java.util.Set;

/**
 * Provide an interface for which two-player game experience boards are expected to satisfy. Chess
 * and Checkers are the first two games to use (implement) this interface.
 *
 * @author Paul Michael Reilly on 2/17/2017
 */

public interface Board {

    /** Clear the selected piece. */
    void clearSelectedPiece();

    /** Return and remove the piece at the given position from the board. */
    Piece delete(int position);

    /** Return a set of position keys in the board model. */
    Set<String> getKeySet();

    /** Return the team associated with the piece at the given position. */
    Team getTeam(final int position);

    /** Return the piece. */
    Piece getPiece(final int position);

    /** Return -1 or the board position corresponding to a given key. */
    int getPosition(final String key);

    /** Return a list of possible moves. */
    List<Integer> getPossibleMoves();

    /** Return null or the currently selected piece. */
    Piece getSelectedPiece();

    /** Return -1 or the selected position. */
    int getSelectedPosition();

    /** Return TRUE iff there is a piece at the given position. */
    boolean hasPiece(final int position);

    /** Return TRUE iff there is a currently selected piece. */
    boolean hasSelectedPiece();

    /** Return true iff the piece is highlighted. */
    boolean isHighlighted(int position);

    /** Set the selected position to the given position. */
    void setSelectedPosition(int position);
}
