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

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.pajato.android.gamechat.exp.Board;
import com.pajato.android.gamechat.exp.Checkerboard;
import com.pajato.android.gamechat.exp.Team;
import com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
 * @author Sandy Scott
 * @author Paul Michael Reilly
 */
public class ChessBoard implements Board {

    // Private class constants.

    private static final String CELL_ID = "cell";

    /** logcat TAG */
    @SuppressWarnings("unused")
    private static final String TAG = ChessBoard.class.getSimpleName();

    // Public instance variables.

    /** The database model for a chess board. */
    private Map<String, ChessPiece> pieces = new HashMap<>();

    // Public constructors.

    /** Build the no-arg instance for Firebase. */
    @SuppressWarnings("unused") public ChessBoard() {}

    /** Build a default instance with an empty pieces. */
    public ChessBoard(@NonNull final Context context, final Chess model, final Checkerboard board) {
        // Create a new board model and reset the castling booleans.
        //model.board = this;
        model.primaryQueenSideRookHasMoved = false;
        model.primaryKingSideRookHasMoved = false;
        model.primaryKingHasMoved = false;
        model.secondaryQueenSideRookHasMoved = false;
        model.secondaryKingSideRookHasMoved = false;
        model.secondaryKingHasMoved = false;

        // Initialize the text on each piece for the start of a game.
        for (int index = 0; index < 16; index++)
            board.initBoardModel(context, index, this);
        for (int index = 48; index < 64; index++)
            board.initBoardModel(context, index, this);
    }

    // Public instance methods.

    /**
     * Add a piece to the board
     * @param index index into the board (valid indices are 0->63).
     * @param type piece type
     * @param team the team
     */
    public void add(final int index, final PieceType type, final Team team) {
        pieces.put(CELL_ID + String.valueOf(index), new ChessPiece(type, team));
    }

    /**
     * Add the specified piece to the board at the indicated location
     * @param index board cell index (valid indices are 0->63)
     * @param p the piece to add
     */
    public void add(final int index, final ChessPiece p) {
        pieces.put(CELL_ID + String.valueOf(index), p);
    }

    /**
     * Convenience method to determine if the primary team's king is at the specified cell location
     * @return true if the primary king is at the specified index
     */
    public boolean containsPrimaryKing() {
        for (int i = 0; i < 64; i++) {
            if (cellHasTeamPiece(i, KING, Team.PRIMARY)) {
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
            if (cellHasTeamPiece(i, KING, Team.SECONDARY)) {
                return true;
            }
        }
        return false;
    }

    /** ... */
    public boolean containsPiece(final int index) {
        return (pieces.get(CELL_ID + String.valueOf(index)) == null);
    }

    /**
     * Remove a piece from the board at the specified index.
     * @param index index into the board (valid indices are 0->63).
     * @return the removed piece
     */
    public ChessPiece delete(final int index) {
        return pieces.remove(CELL_ID + String.valueOf(index));
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
        PieceType type = getPieceType(position);
        if (type == null)
            return PieceType.NONE.text;
        return type.text;
    }

    /** Return a set of position keys representing active pieces on the board. */
    @Exclude @Override public Set<String> getKeySet() {
        return pieces.keySet();
    }

    /** Return null or a chess piece at a given index. */
    public ChessPiece getPiece(final int index) {
        return pieces.get(CELL_ID + String.valueOf(index));
    }

    /**
     * Return the piece type for the piece at the specified index.
     * @param index index into the board (valid indices are 0->63).
     * @return the piece type for the piece at the specified location or empty string ("") if none
     */
    public PieceType getPieceType(final int index) {
        ChessPiece p = pieces.get(CELL_ID + String.valueOf(index));
        if (p == null) {
            return PieceType.NONE;
        } else {
            return p.getPieceType();
        }
    }

    /** Provide a getter for the piece map to satisfy Firebase. */
    @SuppressWarnings("unused") public Map<String, ChessPiece> getPieces() {
        return pieces;
    }

    /** Implement the interface to return -1 or a position corresponding to a given cell key. */
    @Override public int getPosition(@NonNull final String key) {
        try {
            return Integer.parseInt(key.substring(CELL_ID.length()));
        } catch (NumberFormatException exc) {
            return -1;
        }
    }

    /** Return null or the team associated with the piece at the given position. */
    public Team getTeam(final int index) {
        ChessPiece p = pieces.get(CELL_ID + String.valueOf(index));
        if (p == null) {
            return Team.NONE;
        }
        return p.getTeam();
    }

    /** Implement the interface to the typeface corresponding to a given cell key. */
    @Override public int getTypeface(final int position) {
        ChessPiece piece = pieces.get(CELL_ID + String.valueOf(position));
        if (piece == null)
            return PieceType.NONE.typeface;
        return piece.getPieceType().typeface;
    }

    /** Implement the Board interface to setup the default piece at a given position. */
    @Override public void setDefault(final int position) {
        String cellId = "cell" + String.valueOf(position);
        switch (position) {
            case 0: case 7:     // Handle the secondary team (black) rooks.
                pieces.put(cellId, new ChessPiece(ROOK, Team.SECONDARY));
                break;
            case 1: case 6:     // Handle the secondary team (black) knights.
                pieces.put(cellId, new ChessPiece(KNIGHT, Team.SECONDARY));
                break;
            case 2: case 5:     // Handle the secondary team (black) bishops.
                pieces.put(cellId, new ChessPiece(BISHOP, Team.SECONDARY));
                break;
            case 3:             // Handle the secondary team (black) queen.
                pieces.put(cellId, new ChessPiece(QUEEN, Team.SECONDARY));
                break;
            case 4:             // Handle the secondary team (black) king.
                pieces.put(cellId, new ChessPiece(KING, Team.SECONDARY));
                break;
            case 8: case 9: case 10: case 11: case 12: case 13: case 14: case 15:
                // Handle the secondary team (black) pawns.
                pieces.put(cellId, new ChessPiece(PAWN, Team.SECONDARY));
                break;
            case 48: case 49: case 50: case 51: case 52: case 53: case 54: case 55:
                // Handle the primary team (white) pawns.
                pieces.put(cellId, new ChessPiece(PAWN, Team.PRIMARY));
                break;
            case 56: case 63:   // Handle the primary team (white) rooks.
                pieces.put(cellId, new ChessPiece(ROOK, Team.PRIMARY));
                break;
            case 57: case 62:   // Handle the primary team (white) knights.
                pieces.put(cellId, new ChessPiece(KNIGHT, Team.PRIMARY));
                break;
            case 58: case 61:   // Handle the primary team (white) bishops.
                pieces.put(cellId, new ChessPiece(BISHOP, Team.PRIMARY));
                break;
            case 59:            // Handle the primary team (white) queen.
                pieces.put(cellId, new ChessPiece(QUEEN, Team.PRIMARY));
                break;
            case 60:            // Handle the primary team (white) king.
                pieces.put(cellId, new ChessPiece(KING, Team.PRIMARY));
                break;
            default:
                break;
        }
    }

    /** Provide a setter for the piece map to satisfy Firebase. */
    @SuppressWarnings("unused") public void setPieces(final Map<String, ChessPiece> pieceMap) {
        pieces = pieceMap;
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("pieces", pieces);
        return result;
    }

    // Private instance methods.

    /**
     * Determine if specified piece type for the specified team is at the cell location indicates
     * @return true if the primary king is at the specified index
     */
    private boolean cellHasTeamPiece(final int index, PieceType type, Team team) {
        return getPieceType(index).equals(type) && getTeam(index).equals(team);
    }
}
