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

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.exp.Board;
import com.pajato.android.gamechat.exp.Team;
import com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.pajato.android.gamechat.exp.Team.PRIMARY;
import static com.pajato.android.gamechat.exp.Team.SECONDARY;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.BISHOP;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.KING;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.KNIGHT;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.PAWN;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.QUEEN;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.ROOK;

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
 *
 * @author Sandy Scott on 1/9/2017
 * @author Paul Michael Reilly on 2/17/2017
 */
@IgnoreExtraProperties public class ChessBoard implements Board {

    // Private class constants.

    private static final String CELL_ID = "cell";

    /** logcat TAG */
    @SuppressWarnings("unused")
    private static final String TAG = ChessBoard.class.getSimpleName();

    // Private instance variables.

    /** The database model for a chess board. */
    private Map<String, ChessPiece> mPieceMap = new HashMap<>();

    /** The currently selected piece's position. */
    private int mSelectedPosition = -1;

    // Public constructors.

    /** Build the no-arg instance for Firebase. */
    @SuppressWarnings("unused") public ChessBoard() {}

    // Public instance methods.

    /** Add a piece of the given type and team to board at the given position. */
    public void add(final int position, final PieceType type, final Team team) {
        mPieceMap.put(CELL_ID + String.valueOf(position), new ChessPiece(type, team));
    }

    /** Add a particular piece to the board at the given position. */
    public void add(final int position, final ChessPiece p) {
        mPieceMap.put(CELL_ID + String.valueOf(position), p);
    }

    /** Implement the interface to clear the selected piece. */
    @Override public void clearSelectedPiece() {
        mSelectedPosition = -1;
    }

    /** Return TRUE iff the board contains a primary king. */
    boolean containsPrimaryKing() {
        for (int i = 0; i < 64; i++)
            if (cellHasTeamPiece(i, KING, PRIMARY))
                return true;
        return false;
    }

    /** Return TRUE iff the board contains a secondary king. */
    boolean containsSecondaryKing() {
        for (int i = 0; i < 64; i++)
            if (cellHasTeamPiece(i, KING, SECONDARY))
                return true;
        return false;
    }

    /** Return TRUE iff the board contains a piece at the given position. */
    boolean containsPiece(final int position) {
        return (mPieceMap.get(CELL_ID + String.valueOf(position)) == null);
    }

    /** Implement the interface by returning and removing the piece at the given position. */
    @Override public ChessPiece delete(final int position) {
        return mPieceMap.remove(CELL_ID + String.valueOf(position));
    }

    /** Return a set of position keys representing active pieces on the board. */
    @Exclude @Override public Set<String> getKeySet() {
        return mPieceMap.keySet();
    }

    /** Return null or a chess piece at a given index. */
    @Exclude public ChessPiece getPiece(final int index) {
        return mPieceMap.get(CELL_ID + String.valueOf(index));
    }

    /** Return null or the type of the piece at the given position. */
    @Exclude PieceType getPieceType(final int position) {
        ChessPiece p = mPieceMap.get(CELL_ID + String.valueOf(position));
        if (p == null)
            return PieceType.NONE;
        else
            return p.getPieceType();
    }

    /** Provide a getter for the piece map to satisfy Firebase. */
    @SuppressWarnings("unused")
    public Map<String, ChessPiece> getPieces() {
        return mPieceMap;
    }

    /** Implement the interface to return -1 or a position corresponding to a given cell key. */
    @Exclude @Override public int getPosition(@NonNull final String key) {
        try {
            return Integer.parseInt(key.substring(CELL_ID.length()));
        } catch (NumberFormatException exc) {
            return -1;
        }
    }

    /** Implement the interface to return null or the selected piece. */
    @Exclude @Override public ChessPiece getSelectedPiece() {
        return mSelectedPosition >= 0 ? getPiece(mSelectedPosition) : null;
    }

    /** Implement the interface to return null or the selected piece. */
    @Override public int getSelectedPosition() {
        return mSelectedPosition;
    }

    /** Return null or the team associated with the piece at the given position. */
    @Exclude public Team getTeam(final int index) {
        ChessPiece p = mPieceMap.get(CELL_ID + String.valueOf(index));
        if (p == null) {
            return Team.NONE;
        }
        return p.getTeam();
    }

    /** Implement the interface by returning TRUE iff there is a piece at the given position. */
    @Exclude @Override public boolean hasPiece(final int position) {
        return getPiece(position) != null;
    }

    /** Implement the interface to return TRUE iff there is a selected piece. */
    @Exclude @Override public boolean hasSelectedPiece() {
        return mSelectedPosition >= 0;
    }

    /** Initialize the board. */
    public void init() {
        // Set the primary pieces:
        setPiecesForTeamUsingOffsets(SECONDARY, 0, 8);
        setPiecesForTeamUsingOffsets(PRIMARY, 56, 48);
    }

    /** Provide a setter for the piece map to satisfy Firebase. */
    @SuppressWarnings("unused")
    public void setPieces(final Map<String, ChessPiece> pieceMap) {
        mPieceMap = pieceMap;
    }

    /** Implement the interface to set the piece with the given index as the selected piece. */
    @Override public void setSelectedPosition(final int position) {
        mSelectedPosition = position;
    }

    // Private instance methods.

    /** Return TRUE iff the piece at the given position is of the given type and team. */
    private boolean cellHasTeamPiece(final int index, PieceType type, Team team) {
        return getPieceType(index).equals(type) && getTeam(index).equals(team);
    }

    /** Initialize the pieces for a given team using the given offset to calculate positions. */
    private void setPiecesForTeamUsingOffsets(final Team team, final int row1, final int row2) {
        setPieceAtPositions(ROOK, team, row1, 0, 7);
        setPieceAtPositions(KNIGHT, team, row1, 1, 6);
        setPieceAtPositions(BISHOP, team, row1, 2, 5);
        setPieceAtPositions(QUEEN, team, row1, 3);
        setPieceAtPositions(KING, team, row1, 4);
        setPieceAtPositions(PAWN, team, row2, 0, 1, 2, 3, 4, 5, 6, 7);
    }

    /** Initialize one more pieces of given type and team at the given indexed positions. */
    private void setPieceAtPositions(final ChessPiece.PieceType type, final Team team,
                                     final int offset, int... positions) {
        for (int index : positions)
            mPieceMap.put(CELL_ID + String.valueOf(offset + index), new ChessPiece(type, team));
    }
}
