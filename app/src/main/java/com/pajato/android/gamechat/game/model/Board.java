/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.game.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Provide a pojo repesenting a tictactoe board: a map associating buttons and player symbols (X's
 * and O's) and a map providing a running tally of the win possibilities.
 *
 * There are eight win possibilities in TicTacToe: three consecutive identical symbols in each row,
 * each column or along one of the two diagonals.  The app detects one of these eight states by
 * assigning a value of 1 to each X symbol and a value of 4 to each O value.  For X to win, one of
 * the eight tallies must add up to 3 and for O to win one of the eight tallies must add up to 12.
 *
 * Credit for the basic algorithm goes to Bryan Scott.
 *
 * @author Paul Michael Reilly
 */
public class Board {

    // Public win state key constants.

    /** The top row tally key. */
    public static final String TOP_ROW = "topRow";

    /** The middle row tally key. */
    public static final String MID_ROW = "midRow";

    /** The bottom row tally key. */
    public static final String BOT_ROW = "botRow";

    /** The start column tally key. */
    public static final String BEG_COL = "begCol";

    /** The center column tally key. */
    public static final String MID_COL = "midCol";

    /** The end column tally key. */
    public static final String END_COL = "endCol";

    /** The left diagonal tally key. */
    public static final String LEFT_DIAG = "leftDiag";

    /** The right diagonal tally key. */
    public static final String RIGHT_DIAG = "rightDiag";

    // Public instance variables.

    /** The board grid as a map of button tag to symbol value (X or O). */
    public Map<String, String> grid = new HashMap<>();

    /** The board tallies as a map associating key with running integer values. */
    public Map<String, Integer> tallies = new HashMap<>();

    // Public instance methods.

    /** Provide a convenience method to reset the maps. */
    public void clear() {
        grid.clear();
        tallies.clear();
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("grid", grid);
        result.put("tallies", tallies);

        return result;
    }
}
