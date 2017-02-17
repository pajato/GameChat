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

import android.graphics.Typeface;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.exp.Team;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple P.O.J.O. class that keeps track of a chess pieces type and the team it is on.
 */
@IgnoreExtraProperties public class ChessPiece {

    /** Provide constants for chess pieces that use unicode values for the glyph. */
    public enum PieceType {
        NONE (""),
        KING ("\u2654"),
        QUEEN ("\u2655"),
        BISHOP ("\u2657"),
        KNIGHT ("\u2658"),
        ROOK ("\u2656"),
        PAWN ("\u2659");

        public String text;
        public int typeface;

        PieceType(final String text) {
            this.text = text;
            typeface = Typeface.NORMAL;
        }
   }

    /** The chess piece type. */
    private PieceType mPieceType;

    /** The chess team. */
    private Team mTeam;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    @SuppressWarnings("unused") public ChessPiece() {}

    /** Constructor requires a piece and team specified, provided as constants in the class. */
    ChessPiece(final PieceType pieceType, final Team team) {
        mPieceType = pieceType;
        mTeam = team;
    }

    // Public instance methods.

    /** Return TRUE iff this piece is of the given type is playing for the given team. */
    public boolean isTeamPiece(final PieceType p, final Team t) {
        return (mPieceType.equals(p) && mTeam.equals(t));
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("pieceType", mPieceType.name());
        result.put("team", mTeam.name());
        return result;
    }

    public PieceType getPieceType() {
        return mPieceType;
    }

    public Team getTeam() {
        return mTeam;
    }

    /** Provide a setter for Firebase. */
    @SuppressWarnings("unused") public void setPieceType(final String pieceName) {
        mPieceType = PieceType.valueOf(pieceName);
    }

    /** Provide a setter for Firebase. */
    public void setTeam(final String teamName) {
        mTeam = Team.valueOf(teamName);
    }
}
