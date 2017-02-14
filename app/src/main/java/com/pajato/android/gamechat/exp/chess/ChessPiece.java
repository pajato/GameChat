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

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple P.O.J.O. class that keeps track of a chess pieces type and the team it is on.
 */
@IgnoreExtraProperties
public class ChessPiece {

    public enum ChessTeam {
        NONE, PRIMARY, SECONDARY
    }

    public enum PieceType {
        NONE, KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN
   }

    // Unicode value for king
    private static final String UC_KING = "\u2654";

    // Unicode value for queen
    public static final String UC_QUEEN = "\u2655";

    // Unicode value for bishop
    public static final String UC_BISHOP = "\u2657";

    // Unicode value for knight
    public static final String UC_KNIGHT = "\u2658";

    // Unicode value for rook
    public static final String UC_ROOK = "\u2656";

    // Unicode value for pawn
    private static final String UC_PAWN = "\u2659";

    // Access must be public for Firebase use
    public PieceType pieceType;

    // Access must be public for Firebase use
    public ChessTeam teamId;

    /** Build an empty args constructor for the database. */
    @SuppressWarnings("unused") public ChessPiece() {}

    /** Constructor requires a piece and team specified, provided as constants in the class. */
    public ChessPiece(final PieceType pieceType, final ChessTeam teamId) {
        this.pieceType = pieceType;
        this.teamId = teamId;
    }

    public boolean isTeamPiece(final PieceType p, final ChessTeam t) {
        return (this.getPiece().equals(p) && this.getTeam().equals(t));
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("pieceType", pieceType);
        result.put("teamId", teamId);
        return result;
    }

    @Exclude
    public PieceType getPiece() {
        return this.pieceType;
    }

    @Exclude
    public ChessTeam getTeam() {
        return this.teamId;
    }

    /**
     * Returns a specific drawable for the piece type specified in the parameter.
     *
     * @param pieceType the piece type, all of which are available as public constants in this class
     * @return a drawable ID that corresponds to the parameter.
     */
    @Exclude
    public static String getUnicodeText(final PieceType pieceType) {
        String ucText;
        switch(pieceType) {
            case KING: ucText = UC_KING;
                break;
            case QUEEN: ucText = UC_QUEEN;
                break;
            case BISHOP: ucText = UC_BISHOP;
                break;
            case KNIGHT: ucText = UC_KNIGHT;
                break;
            case ROOK: ucText = UC_ROOK;
                break;
            default:
            case PAWN: ucText = UC_PAWN;
                break;
        }
        return ucText;
    }


}
