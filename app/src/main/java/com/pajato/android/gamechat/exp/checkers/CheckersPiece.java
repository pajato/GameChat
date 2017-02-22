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

package com.pajato.android.gamechat.exp.checkers;

import android.graphics.Typeface;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.exp.GameType;
import com.pajato.android.gamechat.exp.Piece;
import com.pajato.android.gamechat.exp.Team;

/**
 * A simple P.O.J.O. class that keeps track of a chess pieces type and the team it is on.
 */
@IgnoreExtraProperties class CheckersPiece implements Piece {

    /** Provide constants for chess pieces that use unicode values for the glyph. */
    enum PieceType implements GameType {
        NONE (""),
        KING ("\u26c1"),
        PIECE ("\u26c0");

        public String text;
        public int typeface;

        PieceType(final String text) {
            this.text = text;
            typeface = Typeface.BOLD;
        }
   }

    /** The checkers piece type for this piece. */
    private PieceType pieceType;

    /** The checkers team for this piece. */
    private Team mTeam;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    @SuppressWarnings("unused") public CheckersPiece() {}

    /** Constructor requires a piece and team specified, provided as constants in the class. */
    CheckersPiece(final PieceType pieceType, final Team team) {
        this.pieceType = pieceType;
        mTeam = team;
    }

    // Public instance methods.

    /** Return the piece type for this piece. */
    @Exclude @Override public PieceType getPieceType() {
        return pieceType;
    }

    /** Implement the interface by returning the name associated with the piece. */
    public String getName() {
        return pieceType.name();
    }

    /** Implement the interface by returning the (unicode) text associated with the piece. */
    @Override public String getText() {
        return pieceType.text;
    }

    /** Implement the interface by returning the team associated with the piece. */
    @Override public Team getTeam() {
        return mTeam;
    }

    /** Implement the interface by returning the typeface associated with the piece. */
    @Override public int getTypeface() {
        return pieceType.typeface;
    }

    /** Implement the interface by returning TRUE iff this piece is of the given type and team. */
    @Exclude @Override public boolean isPiece(final GameType p, final Team t) {
        return (p instanceof PieceType && pieceType.equals(p) && mTeam.equals(t));
    }

    /** Implement the interface by returning TRUE iff this piece is of the given type and team. */
    @Exclude @Override public boolean isType(final GameType type) {
        return type instanceof PieceType && pieceType.equals(type);
    }

    public void setName(final String value) {
        pieceType = PieceType.valueOf(value);
    }

    /** Provide a setter for Firebase. */
    @SuppressWarnings("unused")
    @Exclude public void setPieceType(final String pieceName) {
        pieceType = PieceType.valueOf(pieceName);
    }

    /** Provide a setter for Firebase. */
    public void setTeam(final String teamName) {
        mTeam = Team.valueOf(teamName);
    }
}
