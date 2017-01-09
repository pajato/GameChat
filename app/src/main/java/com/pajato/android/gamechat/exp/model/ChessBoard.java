package com.pajato.android.gamechat.exp.model;

import com.pajato.android.gamechat.exp.ChessPiece;
import com.pajato.android.gamechat.exp.ChessPiece.PieceType;
import com.pajato.android.gamechat.exp.ChessPiece.ChessTeam;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.type;

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

public class ChessBoard {

    private static final String CELL_ID = "cell";

    /** logcat TAG */
    private static final String TAG = ChessBoard.class.getSimpleName();

    public Map<String, ChessPiece> board;

    private String makeCellId(final int index) {
        return CELL_ID + String.valueOf(index);
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("board", board);
        return result;
    }

    public ChessBoard() {
        board = new HashMap<>();
    }

    /**
     * Add a piece to the board
     * @param index index into the board (valid indices are 0->63).
     * @param type piece type
     * // TODO: change this to take one of the ChessTeam enum values
     * @param team the team (0 = primary team, 1 = secondary team)
     */
    public void add(final int index, final PieceType type, final ChessTeam team) {
        board.put(makeCellId(index), new ChessPiece(type, team));
    }

    /**
     * Add the specified piece to the board at the indicated location
     * @param index board cell index (valid indices are 0->63)
     * @param p the piece to add
     */
    public void add(final int index, final ChessPiece p) {
        board.put(makeCellId(index), p);
    }

    /**
     * Get a piece from the board at the specified index.
     * @return ChessPiece at the specified cell index or null if none.
     */
    public ChessPiece retrieve(final int index) {
        return board.get(makeCellId(index));
    }

    /**
     * Remove a piece from the board at the specified index.
     * @param index index into the board (valid indices are 0->63).
     * @return the removed piece
     */
    public ChessPiece delete(final int index) {
        return board.remove(makeCellId(index));
    }

    public boolean containsPiece(final int index) {
        return (board.get(makeCellId(index)) == null);
    }

    /**
     * Determine if specified peice type for the specified team is at the cell location indicates
     * @return true if the primary king is at the specified index
     */
    private boolean cellHasTeamPiece(final int index, PieceType type, ChessTeam team) {
        return getPieceType(index).equals(type) && getTeam(index).equals(team);
    }

    /**
     * Convenience method to determine if the primary team's king is at the specified cell location
     * @return true if the primary king is at the specified index
     */
    public boolean containsPrimaryKing() {
        for (int i = 0; i < 64; i++) {
            if (cellHasTeamPiece(i, PieceType.KING, ChessTeam.PRIMARY)) {
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
            if (cellHasTeamPiece(i, PieceType.KING, ChessTeam.SECONDARY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the piece type for the piece at the specified index.
     * @param index index into the board (valid indices are 0->63).
     * @return the piece type for the piece at the specified location or empty string ("") if none
     */
    public PieceType getPieceType(final int index) {
        ChessPiece p = board.get(makeCellId(index));
        if (p == null) {
            return PieceType.NONE;
        } else {
            return p.getPiece();
        }
    }

    /**
     * Return the team id for the piece at the specified index.
     * TODO: Refactor the ChessFragment, ChessHelper & friends to use the enum value, not an int!!
     * @param index index into the board (valid indices are 0->63).
     * @return the team for any piece at the specified location or -1 if none
     */
    public ChessTeam getTeam(final int index) {
        ChessPiece p = board.get(makeCellId(index));
        if (p == null) {
            return ChessTeam.NONE;
        }
        return p.getTeam();
    }
}

