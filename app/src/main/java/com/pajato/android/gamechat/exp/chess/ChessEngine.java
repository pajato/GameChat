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

package com.pajato.android.gamechat.exp.chess;

import android.support.annotation.NonNull;

/**
 * Provides a class to support playing chess alone, against another User and against the computer.
 *
 * Termiology:
 *
 * Active: refers to the player (or one of his/her pieces) whose turn it is to make a move.
 * Passive: refers to the player (or one of his/her pieces) who has just made a move.
 *
 * @author Paul Michael Reilly on Feb 14, 2017
 */

public class ChessEngine {
    // Public class constants.

    // Private class constants.

    // Public instance variables.

    /** The chess model on which the engine operates. */
    public Chess model;

    // Private instance variables.

    // Public constructors.

    // Public instance methods.

    /** Return true iff the active player is in check. */
    public boolean isInCheck(@NonNull final Chess model) {
        // Determine if any passive piece now threatens the active king.
        return false;
    }

    // Private instance methods.

    // Private inner classes.
}
