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

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.pajato.android.gamechat.exp.Board;
import com.pajato.android.gamechat.exp.Checkerboard;
import com.pajato.android.gamechat.exp.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.pajato.android.gamechat.exp.checkers.CheckersPiece.PieceType.PIECE;

/**
 * Provide a POJO representing a Chess board and methods to modify the board. The basic board is
 * a HashMap of board cell index (0->63) to ChessPiece (which contains team and piece type).
 *
 * Since Firebase has some difficulty with integer-based key values for a HashMap (even if they are
 * String representations in Java), the key to our HashMap will have "index" prepended to the cell
 * index value, so that Firebase cannot ever interpret it as an integer.
 *
 * So, this class is essentially a wrapper around a HashMap which handles the conversion of an
 * integer index to a string and provides some convenience methods to determine piece type and team
 * for a given cell on the board.
 */

public class CheckersBoard implements Board {

    private static final String CELL_ID = "cell";

    /** logcat TAG */
    @SuppressWarnings("unused")
    private static final String TAG = CheckersBoard.class.getSimpleName();

    public Map<String, CheckersPiece> pieceMap;

    private String makeCellId(final int index) {
        return CELL_ID + String.valueOf(index);
    }

    // Public constructors.

    /** Provide a no-arg constructor for Firebase. */
    public CheckersBoard() {}

    /** Build an instance that initializes the underlying board. */
    public CheckersBoard(@NonNull final Context context, final Checkerboard board) {
        pieceMap = new HashMap<>();

        // Initialize the text on each piece for the start of a game.
        for (int index = 0; index < 24; index++)
            board.initBoardModel(context, index, this);
        for (int index = 40; index < 64; index++)
            board.initBoardModel(context, index, this);

    }

    /**
     * Add a piece to the board
     * @param index index into the board (valid indices are 0->63).
     * @param type piece type
     * @param team the team
     */
    public void add(final int index, final CheckersPiece.PieceType type, final Team team) {
        pieceMap.put(makeCellId(index), new CheckersPiece(type, team));
    }

    /**
     * Add the specified piece to the board at the indicated location
     * @param index board cell index (valid indices are 0->63)
     * @param p the piece to add
     */
    public void add(final int index, final CheckersPiece p) {
        pieceMap.put(makeCellId(index), p);
    }

    /**
     * Convenience method to determine if the primary team's king is at the specified cell location
     * @return true if the primary king is at the specified index
     */
    public boolean containsPrimaryKing() {
        for (int i = 0; i < 64; i++) {
            if (cellHasTeamPiece(i, CheckersPiece.PieceType.KING, Team.PRIMARY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method to determine if the secondary team's king is at the specified cell location
     * @return true if the secondary king is at the specified index
     */
    public boolean containsSecondaryKing() {
        for (int i = 0; i < 64; i++) {
            if (cellHasTeamPiece(i, CheckersPiece.PieceType.KING, Team.SECONDARY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a piece from the board at the specified index.
     * @param index index into the board (valid indices are 0->63).
     * @return the removed piece
     */
    public CheckersPiece delete(final int index) {
        return pieceMap.remove(makeCellId(index));
    }

    /** Implement the Board interface to return the default piece text color at a given position. */
    @Override public int getDefaultColor(final int position) {
        Team team = getTeam(position);
        if (team == null)
            return Team.NONE.color;
        return team.color;
    }

    /** Implement the Board interface to return the default piece text value at a given position. */
    @Override public String getDefaultText(final int position) {
        CheckersPiece.PieceType type = getPieceType(position);
        if (type == null)
            return CheckersPiece.PieceType.NONE.text;
        return type.text;
    }

    /** Return a set of position keys representing active pieces on the board. */
    @Exclude @Override public Set<String> getKeySet() {
        return pieceMap.keySet();
    }

    /** Return a checkers piece given a board index. */
    public CheckersPiece getPiece(final int index) {
        return pieceMap.get(makeCellId(index));
    }

    /** Return a checkers piece given a cell key. */
    public CheckersPiece getPiece(final String key) {
        return pieceMap.get(key);
    }

    /**
     * Return the piece type for the piece at the specified index.
     * @param index index into the board (valid indices are 0->63).
     * @return the piece type for the piece at the specified location or empty string ("") if none
     */
    public CheckersPiece.PieceType getPieceType(final int index) {
        CheckersPiece p = pieceMap.get(makeCellId(index));
        if (p == null) {
            return CheckersPiece.PieceType.NONE;
        } else {
            return p.getPieceType();
        }
    }

    /** Implement the interface to return -1 or a position corresponding to a given cell key. */
    @Override public int getPosition(@NonNull final String key) {
        try {
            return Integer.parseInt(key.substring(CELL_ID.length()));
        } catch (NumberFormatException exc) {
            return -1;
        }
    }

    /**
     * Return the team id for the piece at the specified index.
     * TODO: Refactor the ChessFragment, ChessHelper & friends to use the enum value, not an int!!
     * @param index index into the board (valid indices are 0->63).
     * @return the team for any piece at the specified location or -1 if none
     */
    public Team getTeam(final int index) {
        CheckersPiece p = pieceMap.get(makeCellId(index));
        if (p == null) {
            return Team.NONE;
        }
        return p.getTeam();
    }

    /** Implement the interface to the typeface corresponding to a given cell key. */
    @Override public int getTypeface(final int position) {
        CheckersPiece piece = pieceMap.get(CELL_ID + String.valueOf(position));
        if (piece == null)
            return CheckersPiece.PieceType.NONE.typeface;
        return piece.getPieceType().typeface;
    }

    /**
     * Determine if specified piece type for the specified team is at the cell location indicates
     * @return true if the primary king is at the specified index
     */
    public boolean cellHasTeamPiece(final int index, CheckersPiece.PieceType type, Team team) {
        return getPieceType(index).equals(type) && getTeam(index).equals(team);
    }

    /** Implement the Board interface to setup the default piece at a given position. */
    @Override public void setDefault(final int position) {
        String cellId = "cell" + String.valueOf(position);
        switch (position) {
            case 0: case 2: case 4: case 6:
            case 9: case 11: case 13: case 15:
            case 16: case 18: case 20: case 22:
                // Handle the secondary team (black) rooks.
                pieceMap.put(cellId, new CheckersPiece(PIECE, Team.SECONDARY));
                break;
            case 40: case 42: case 44: case 46:
            case 49: case 51: case 53: case 55:
            case 56: case 58: case 60: case 62:
                // Handle the primary team (white) pawns.
                pieceMap.put(cellId, new CheckersPiece(PIECE, Team.PRIMARY));
                break;
            default:
                break;
        }
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("board", pieceMap);
        return result;
    }
}
