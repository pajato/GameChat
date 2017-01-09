package com.pajato.android.gamechat.exp.model;

import com.pajato.android.gamechat.exp.ChessPiece;

import java.util.Map;

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

    // TODO: Usen this enum instead of integer values for team
    private enum ChessTeam {
        NONE, PRIMARY, SECONDARY;
    }

    private static final String CELL_ID = "cell";

    /** logcat TAG */
    private static final String TAG = ChessBoard.class.getSimpleName();

    public Map<String, ChessPiece> board;

    private String makeCellId(final int index) {
        return CELL_ID + String.valueOf(index);
    }

    /**
     * Add a piece to the board
     * @param index index into the board (valid indices are 0->63).
     * @param type piece type
     * // TODO: change this to take one of the ChessTeam enum values
     * @param team the team (0 = primary team, 1 = secondary team)
     */
    public void add(final int index, final String type, final int team) {
        board.put(makeCellId(index), new ChessPiece(type, team));
    }

    /**
     * Get a piece from the board at the specified index.
     * @return ChessPiece at the specified cell index or null if none.
     */
    public ChessPiece get(final int index) {
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

    /**
     * Return the piece type for the piece at the specified index.
     * @param index index into the board (valid indices are 0->63).
     * @return the piece type for the piece at the specified location or empty string ("") if none
     */
    public String getPieceType(final int index) {
        ChessPiece p = board.get(makeCellId(index));
        if (p == null) {
            return "";
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
    public int getTeam(final int index) {
        ChessPiece p = board.get(makeCellId(index));
        if (p == null) {
            return -1;
        }

        if (p.getTeam() == ChessHelper.PRIMARY_TEAM) {
            return 0;
        } else if (p.getTeam() == ChessHelper.SECONDARY_TEAM) {
            return 1;
        } else {
            return -1;
        }
    }
}

