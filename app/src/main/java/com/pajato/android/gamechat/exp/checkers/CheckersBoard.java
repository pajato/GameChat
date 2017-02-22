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

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.pajato.android.gamechat.exp.Board;
import com.pajato.android.gamechat.exp.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.pajato.android.gamechat.exp.Team.PRIMARY;
import static com.pajato.android.gamechat.exp.Team.SECONDARY;

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

    private Map<String, CheckersPiece> mPieceMap = new HashMap<>();

    /** The currently selected piece's position. */
    private int mSelectedPosition = -1;

    // Public constructors.

    /** Provide a no-arg constructor for Firebase. */
    @SuppressWarnings("unused") public CheckersBoard() {}

    /** Add a piece of the given type and team to board at the given position. */
    public void add(final int index, final CheckersPiece.PieceType type, final Team team) {
        mPieceMap.put(makeCellId(index), new CheckersPiece(type, team));
    }

    /** Add a particular piece to the board at the given position. */
    public void add(final int index, final CheckersPiece p) {
        mPieceMap.put(makeCellId(index), p);
    }

    /** Implement the interface to clear the selected piece. */
    @Override public void clearSelectedPiece() {
        mSelectedPosition = -1;
    }

    /**
     * Remove a piece from the board at the specified index.
     * @param index index into the board (valid indices are 0->63).
     * @return the removed piece
     */
    public CheckersPiece delete(final int index) {
        return mPieceMap.remove(makeCellId(index));
    }

    /** Return a set of position keys representing active pieces on the board. */
    @Exclude @Override public Set<String> getKeySet() {
        return mPieceMap.keySet();
    }

    /** Return a checkers piece given a board index. */
    public CheckersPiece getPiece(final int index) {
        return mPieceMap.get(makeCellId(index));
    }

    /** Provide a getter for the piece map to satisfy Firebase. */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, CheckersPiece> getPieces() {
        return mPieceMap;
    }

    /** Implement the interface to return -1 or a position corresponding to a given cell key. */
    @Override public int getPosition(@NonNull final String key) {
        try {
            return Integer.parseInt(key.substring(CELL_ID.length()));
        } catch (NumberFormatException exc) {
            return -1;
        }
    }

    /** Implement the interface to return null or the selected piece. */
    @Exclude @Override public CheckersPiece getSelectedPiece() {
        return mSelectedPosition >= 0 ? getPiece(mSelectedPosition) : null;
    }

    /** Implement the interface to return -1 or the selected position. */
    @Override public int getSelectedPosition() {
        return mSelectedPosition;
    }

    /**
     * Return the team id for the piece at the specified index.
     * TODO: Refactor the ChessFragment, ChessHelper & friends to use the enum value, not an int!!
     * @param index index into the board (valid indices are 0->63).
     * @return the team for any piece at the specified location or -1 if none
     */
    @Exclude @Override public Team getTeam(final int index) {
        CheckersPiece p = mPieceMap.get(makeCellId(index));
        if (p == null) {
            return Team.NONE;
        }
        return p.getTeam();
    }

    /** Implement the interface by returning TRUE iff there is a piece at the given position. */
    @Override public boolean hasPiece(final int position) {
        return getPiece(position) != null;
    }

    /** Implement the interface to return TRUE iff there is a selected piece. */
    @Override public boolean hasSelectedPiece() {
        return mSelectedPosition >= 0;
    }

    /** Initialize the board. */
    public void init() {
        // Set the pieces for each team.
        setPiecesForTeam(SECONDARY, 1, 3, 5, 7, 8, 10, 12, 14, 17, 19, 21, 23);
        setPiecesForTeam(PRIMARY, 40, 42, 44, 46, 49, 51, 53, 55, 56, 58, 60, 62);
    }

    /** Provide a setter for the piece map to satisfy Firebase. */
    @SuppressWarnings("unused")
    public void setPieces(final Map<String, CheckersPiece> pieceMap) {
        mPieceMap = pieceMap;
    }

    /** Implement the interface to set the piece with the given index as the selected piece. */
    @Override public void setSelectedPosition(final int position) {
        mSelectedPosition = position;
    }

    // Private instance methods.

    /** Generate a Firebase safe position key (not a pure integer). */
    private String makeCellId(final int position) {
        return CELL_ID + String.valueOf(position);
    }

    /** Initialize one more pieces of given type and team at the given indexed positions. */
    private void setPiecesForTeam(final Team team, final int... positions) {
        CheckersPiece piece = new CheckersPiece(CheckersPiece.PieceType.PIECE, team);
        for (int position : positions)
            mPieceMap.put(CELL_ID + String.valueOf(position), piece);
    }
}
