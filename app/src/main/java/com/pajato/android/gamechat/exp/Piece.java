package com.pajato.android.gamechat.exp;

/**
 * Provide an abstraction for a game piece, particularly chess and checkers.
 *
 * @author Paul Michael Reilly on 2/18/17.
 */

public interface Piece {

    /** Return the piece name. */
    String getName();

    /** Return the piece text. */
    String getText();

    /** Return the piece type. */
    GameType getPieceType();

    /** Return the team associate with the piece. */
    Team getTeam();

    /** Return the typeface for the piece. */
    int getTypeface();

    /** Return TRUE iff the piece is of the given type and team. */
    boolean isPiece(GameType type, Team team);

    /** Return TRUE iff the type of the piece matches the given type. */
    boolean isType(GameType type);
}
