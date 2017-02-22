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

/**
 * Provides a game engine interface.  Chess and checkers are examples of game that satisfy this
 * interface. The implementing concrete classes generally provide the main logic supporting the
 * game.
 *
 * @author Paul Michael Reilly on Feb 18, 2017
 */
public interface Engine {

    /** Initialize this engine. */
    void init(Experience model, Checkerboard board, TileClickHandler handler);

    /**
     * Process a tile click event on a game board by selecting a piece at the click position
     * (when there is not a piece already selected) and highlighting the possible places to
     * which the selected piece can be moved.  When there is a selected piece, the clicked piece
     * will become the new location for the selected piece if it is highlighted (a valid move),
     * otherwise the clicked piece becomes the new selection.
     *
     * @param position The selected position if there is not one already selected, or the
     *                 position to move the selected piece.
     */
    void processTileClick(int position);
}
