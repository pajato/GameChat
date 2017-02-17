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

import com.pajato.android.gamechat.exp.Team;

import java.util.List;

import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.ROOK;

/**
 * Helper class to determine for chess features.
 */

public class ChessHelper {

    // Threat Range Methods

    /**
     * Gets the "threat range" of the King. A "threat range" is the full possible amount of
     * locations a piece can enter without breaking the rules of chess. The King's movement is
     * limited, and when the King of a side is captured, that side loses the game. A King can move
     * in any direction, but only a single square away. A King may move into the space of another
     * piece only if it is capturing that piece.
     *  @param threatRange the ArrayList that contains the threat range of the King.
     * @param highlightedIndex the index of the current piece.
     * @param board a HashMap representing an index on to board (0->63) the piece type at that location.
     */
    static public void getKingThreatRange(final List<Integer> threatRange, final int highlightedIndex,
                                          final ChessBoard board, final boolean[] castlingBooleans) {
        // Grab all the castling booleans and label them properly.
        final boolean primaryQueenSideRookHasMoved = castlingBooleans[0];
        final boolean primaryKingSideRookHasMoved = castlingBooleans[1];
        final boolean primaryKingHasMoved = castlingBooleans[2];
        final boolean secondaryQueenSideRookHasMoved = castlingBooleans[3];
        final boolean secondaryKingSideRookHasMoved = castlingBooleans[4];
        final boolean secondaryKingHasMoved = castlingBooleans[5];

        // Establish all possible movement options.
        int left = highlightedIndex - 1;
        int upLeft = highlightedIndex - 9;
        int up = highlightedIndex - 8;
        int upRight = highlightedIndex - 7;
        int right = highlightedIndex + 1;
        int down = highlightedIndex + 8;
        int downRight = highlightedIndex + 9;
        int downLeft = highlightedIndex + 7;

        // Ensure we're obeying the margins of the board with our moves.
        if(left % 8 == 7) left = -1;
        if(upLeft % 8 == 7) upLeft = -1;
        if(downLeft % 8 == 7) downLeft = -1;
        if(right % 8 == 0) right = -1;
        if(upRight % 8 == 0) upRight = -1;
        if(downRight % 8 == 0) downRight = -1;

        // Add our valid moves to the possible move pool.
        int[] possibleMoves = {left, upLeft, up, upRight, right, downRight, down, downLeft};
        for (int possibleMove : possibleMoves) {
            if(possibleMove > -1 && possibleMove < 64) {
                if(board.getPiece(possibleMove) == null) {
                    threatRange.add(possibleMove);
                } else if (board.getTeam(possibleMove) != board.getTeam(highlightedIndex)) {
                    threatRange.add(possibleMove);
                }
            }
        }

        // Handle Castling for Blue
        if(board.getTeam(highlightedIndex).equals(Team.PRIMARY) && !primaryKingHasMoved) {
            // The more common Castling variant, "Queen-Side Castling" or "Short Castling"
            boolean canCastleKingSide = highlightedIndex == 60 && !primaryKingSideRookHasMoved &&
                    board.getPiece(highlightedIndex + 1) == null &&
                    board.getPiece(highlightedIndex + 2) == null &&
                    board.getPiece(highlightedIndex + 3) != null &&
                    board.getPieceType(highlightedIndex + 3).equals(ROOK);
            if(canCastleKingSide) {
                threatRange.add(highlightedIndex + 2);
            }
            // The less common Castling variant, "Queen-Side Castling" or "Long Castling"
            boolean canCastleQueenSide = highlightedIndex == 60 && !primaryQueenSideRookHasMoved &&
                    board.getPiece(highlightedIndex - 1) == null &&
                    board.getPiece(highlightedIndex - 2) == null &&
                    board.getPiece(highlightedIndex - 3) == null &&
                    board.getPiece(highlightedIndex - 4) != null &&
                    board.getPieceType(highlightedIndex - 4).equals(ROOK);
            if(canCastleQueenSide) {
                threatRange.add(highlightedIndex - 3);
            }
            // Handle Castling for the other team
        } else if (board.getTeam(highlightedIndex).equals(Team.SECONDARY) && !secondaryKingHasMoved) {
            boolean canCastleKingSide = highlightedIndex == 4 && !secondaryKingSideRookHasMoved &&
                    board.getPiece(highlightedIndex + 1) == null &&
                    board.getPiece(highlightedIndex + 2) == null &&
                    board.getPiece(highlightedIndex + 3) != null &&
                    board.getPieceType(highlightedIndex + 3).equals(ROOK);
            if (canCastleKingSide) {
                threatRange.add(highlightedIndex + 2);
            }
            // The less common Castling variant, "Queen-Side Castling" or "Long Castling"
            boolean canCastleQueenSide = highlightedIndex == 4 && !secondaryQueenSideRookHasMoved &&
                    board.getPiece(highlightedIndex - 1) == null &&
                    board.getPiece(highlightedIndex - 2) == null &&
                    board.getPiece(highlightedIndex - 3) == null &&
                    board.getPiece(highlightedIndex - 4) != null &&
                    board.getPieceType(highlightedIndex - 4).equals(ROOK);
            if(canCastleQueenSide) {
                threatRange.add(highlightedIndex - 3);
            }
        }
    }

    /**
     * Gets the "threat range" of the Queen. A "threat range" is the full possible amount of
     * locations a piece can enter without breaking the rules of chess. In addition to all four
     * diagonals (up/left, up/right, down/right, and down/left), Queens can move in the four
     * cardinal directions (up, down, left, and right), but cannot move past other pieces. A queen
     * may take the place of another piece if they are capturing. They functionally perform as a
     * combination Rook-Bishop.
     *  @param threatRange the ArrayList that contains the threat range of the Queen.
     * @param highlightedIndex the index of the current piece.
     * @param board a HashMap representing the board and the pieces located within.
     */
    static public void getQueenThreatRange(final List<Integer> threatRange, final int
            highlightedIndex, final ChessBoard board) {
        getRookThreatRange(threatRange, highlightedIndex, board);
        getBishopThreatRange(threatRange, highlightedIndex, board);
    }

    /**
     * Gets the "threat range" of the Bishop. A "threat range" is the full possible amount of
     * locations a piece can enter without breaking the rules of chess. Bishops can move across
     * the diagonals (up/right, down/right, down/left, and up/right), but cannot move past other
     * pieces. A bishop can take the place of another piece if they are capturing it.
     *  @param threatRange the ArrayList that contains the threat range of the Bishop.
     * @param highlightedIndex the index of the current piece.
     * @param board a HashMap representing the board and the pieces located within.
     */
    static public void getBishopThreatRange(final List<Integer> threatRange, final int
            highlightedIndex, ChessBoard board) {
        boolean upLeft = true;
        boolean upRight = true;
        boolean downRight = true;
        boolean downLeft = true;

        for(int i = 1; i < 8; i++) {
            if (upLeft) {
                // Stay within the edges of the board.
                int upLeftIteration = highlightedIndex - (9 * i);
                if (upLeftIteration % 8 != 7 && upLeftIteration > -1) {
                    // If there's no piece, a rook can go there and keep going.
                    if (board.getPiece(upLeftIteration) == null) {
                        threatRange.add(upLeftIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (board.getTeam(upLeftIteration) !=  board.getTeam(highlightedIndex)) {
                        threatRange.add(upLeftIteration);
                        upLeft = false;
                        // Otherwise we're done.
                    } else {
                        upLeft = false;
                    }
                    // If we're past the board's edge, stop.
                } else {
                    upLeft = false;
                }
            }
            if (upRight) {
                // Stay within the edges of the board.
                int upRightIteration = highlightedIndex - (7 * i);
                if (upRightIteration % 8 != 0 && upRightIteration > -1) {
                    // If there's no piece, a rook can go there and keep going.
                    if (board.getPiece(upRightIteration) == null) {
                        threatRange.add(upRightIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (board.getTeam(upRightIteration) !=  board.getTeam(highlightedIndex)) {
                        threatRange.add(upRightIteration);
                        upRight = false;
                        // Otherwise we're done.
                    } else {
                        upRight = false;
                    }
                    // If we're past the board's edge, stop.
                } else {
                    upRight = false;
                }
            }
            if (downRight) {
                // Stay within the edges of the board.
                int downRightIteration = highlightedIndex + (9 * i);
                if (downRightIteration % 8 != 0 && downRightIteration < 64) {
                    // If there's no piece, a rook can go there and keep going.
                    if (board.getPiece(downRightIteration) == null) {
                        threatRange.add(downRightIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (board.getTeam(downRightIteration) != board.getTeam(highlightedIndex)) {
                        threatRange.add(downRightIteration);
                        downRight = false;
                        // Otherwise we're done.
                    } else {
                        downRight = false;
                    }
                    // If we're past the board's edge, stop.
                } else {
                    downRight = false;
                }
            }
            if (downLeft) {
                // Stay within the edges of the board.
                int downLeftIteration = highlightedIndex + (7 * i);
                if (downLeftIteration % 8 != 7 && downLeftIteration < 64) {
                    // If there's no piece, a rook can go there and keep going.
                    if (board.getPiece(downLeftIteration) == null) {
                        threatRange.add(downLeftIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (board.getTeam(downLeftIteration) != board.getTeam(highlightedIndex)) {
                        threatRange.add(downLeftIteration);
                        downLeft = false;
                        // Otherwise we're done.
                    } else {
                        downLeft = false;
                    }
                    // If we're past the board's edge, stop.
                } else {
                    downLeft = false;
                }
            }
        }
    }


    /**
     * Gets the "threat range" of the Pawn. A "threat range" is the full possible amount of
     * locations a piece can enter without breaking the rules of chess. Pawns can move forward
     * if there is no piece ahead of them, and diagonally forward if they are capturing a piece.
     *  @param threatRange the ArrayList that contains the threat range of the pawn.
     * @param highlightedIndex the index of the current piece.
     * @param board a HashMap representing the board and the pieces located within.
     */
    static public void getPawnThreatRange(final List<Integer> threatRange, final int highlightedIndex,
                                          final ChessBoard board) {
        // Pawns move differently depending on the team they are on. First, handle the pawns that
        // move "up". These pawns are on the primary team.
        if (board.getTeam(highlightedIndex).equals(Team.PRIMARY)) {
            int upLeft = highlightedIndex - 9;
            int upRight = highlightedIndex - 7;
            int up = highlightedIndex - 8;

            // Pawns can move two squares forward if they are in their starting position.
            if (highlightedIndex > 47 && highlightedIndex < 56) {
                int upUp = highlightedIndex -  16;
                if(board.getPiece(upUp) == null && board.getPiece(up) == null) {
                    threatRange.add(upUp);
                }
            }

            // Pawns can move diagonally forward only if they are capturing a piece.
            if (board.getPiece(upLeft) != null && upLeft % 8 != 7 &&
                    board.getTeam(upLeft).equals(Team.SECONDARY)) {
                threatRange.add(upLeft);
            }
            if (board.getPiece(upRight) != null && upRight % 8 != 0 &&
                    board.getTeam(upRight).equals(Team.SECONDARY)) {
                threatRange.add(upRight);
            }
            // Pawns can move forward only if they are not being blocked.
            if (board.getPiece(up) == null && up > -1) {
                threatRange.add(up);
            }
            // We also need to handle the second team, which moves "downward" on the board.
        } else {
            int downRight = highlightedIndex + 9;
            int downLeft = highlightedIndex + 7;
            int down = highlightedIndex + 8;

            // Pawns can move two squares forward if they are in their starting position.
            if(highlightedIndex > 7 && highlightedIndex < 16) {
                int downDown = highlightedIndex + 16;
                if(board.getPiece(downDown) == null && board.getPiece(down) == null) {
                    threatRange.add(downDown);
                }
            }

            // Pawns can move diagonally forward only if they are capturing a piece.
            if (board.getPiece(downRight) !=null && downRight % 8 != 0
                    && board.getTeam(downRight).equals(Team.PRIMARY)) {
                threatRange.add(downRight);
            }
            if (board.getPiece(downLeft) != null && downRight % 8 != 7
                    && board.getTeam(downLeft).equals(Team.PRIMARY)) {
                threatRange.add(downLeft);
            }
            // Pawns can move forward only if they are not being blocked.
            if (board.getPiece(down) == null && down < 64) {
                threatRange.add(down);
            }
        }
    }

    /**
     * Gets the "threat range" of the Knight. A "threat range" is the full possible amount of
     * locations a piece can enter without breaking the rules of chess. Knights must move in an
     * L shape: two squares in one direction and one in another. They can jump other pieces.
     *  @param threatRange the ArrayList that contains the threat range of the knight piece.
     * @param highlightedIndex the index of the current piece.
     * @param board a HashMap representing the board and the pieces located within.
     */
    static public void getKnightThreatRange(final List<Integer> threatRange, final int
            highlightedIndex, final ChessBoard board) {
        // Establish all possible movement options.
        int upUpLeft = highlightedIndex - 17;
        int upUpRight = highlightedIndex - 15;
        int rightRightUp = highlightedIndex - 6;
        int rightRightDown = highlightedIndex + 10;
        int downDownRight = highlightedIndex + 17;
        int downDownLeft = highlightedIndex + 15;
        int leftLeftDown = highlightedIndex + 6;
        int leftLeftUp = highlightedIndex - 10;

        // Ensure we obey the sides of the board when we set up our movement options.
        if(upUpLeft % 8 == 7) upUpLeft = -1;
        if(upUpRight % 8 == 0) upUpRight = -1;
        if (rightRightUp % 8 == 0 || rightRightUp % 8 == 1) rightRightUp = -1;
        if (rightRightDown % 8 == 0 || rightRightDown % 8 == 1) rightRightDown = -1;
        if (downDownRight % 8 == 0) downDownRight = -1;
        if (downDownLeft % 8 == 7) downDownLeft = -1;
        if (leftLeftDown % 8 == 7 || leftLeftDown % 8 == 6) leftLeftDown = -1;
        if (leftLeftUp % 8 == 7 || leftLeftUp % 8 == 6) leftLeftUp = -1;

        int[] possibleMoves = {upUpLeft, upUpRight, rightRightUp, rightRightDown, downDownRight,
                downDownLeft, leftLeftDown, leftLeftUp};

        // Add valid moves to the movement option pool.
        for (int possibleMove : possibleMoves) {
            if (possibleMove > -1 && possibleMove < 64) {
                if (board.getPiece(possibleMove) == null) {
                    threatRange.add(possibleMove);
                } else if (board.getTeam(possibleMove) != board.getTeam(highlightedIndex)) {
                    threatRange.add(possibleMove);
                }
            }
        }

    }

    /**
     * Gets the "threat range" of the Rook. A "threat range" is the full possible amount of
     * locations a piece can enter without breaking the rules of chess. Rooks can move in the
     * four cardinal directions (up, down, left, and right), but cannot move past other pieces. A
     * rook can take the place of another piece only if it is capturing that piece.
     *
     * @param threatRange the ArrayList that contains the threat range of the rook piece.
     * @param highlightedIndex the index of the current piece.
     * @param board a HashMap representing the board and the pieces located within.
     */
    static public void getRookThreatRange(final List<Integer> threatRange,
                final int highlightedIndex, final ChessBoard board) {
        // Set up our condition variables.
        boolean left = true;
        boolean right = true;
        boolean up = true;
        boolean down = true;
        // Rooks can move at any distance, so we need to take the full board size into account.
        for(int i = 1; i < 8; i++) {
            // Calculates how far we can go left. Other pieces and the board's edge are barriers
            if (left) {
                int leftIteration = highlightedIndex - i;
                // Stay within the edges of the board.
                if (leftIteration % 8 != 7 && leftIteration > -1) {
                    // If there's no piece, a rook can go there and keep going.
                    if (board.getPiece(leftIteration) == null) {
                        threatRange.add(leftIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (board.getTeam(leftIteration) != board.getTeam(highlightedIndex)) {
                        threatRange.add(leftIteration);
                        left = false;
                        // Otherwise we're done.
                    } else {
                        left = false;
                    }
                    // If we're past the board's edge, stop.
                } else {
                    left = false;
                }
            }
            // Calculates how far we can go right. Other pieces and the board's edge are barriers
            if (right) {
                int rightIteration = highlightedIndex + i;
                // Stay within the edges of the board.
                if (rightIteration % 8 != 0 && rightIteration < 64) {
                    // If there's no piece, a rook can go there and keep going.
                    if(board.getPiece(rightIteration) == null) {
                        threatRange.add(rightIteration);
                        // If there's an enemy piece, a rook can go there but no further.
                    } else if (board.getTeam(rightIteration) != board.getTeam(highlightedIndex)) {
                        threatRange.add(rightIteration);
                        right = false;
                        // Otherwise we're done.
                    } else {
                        right = false;
                    }
                    // If we're past the board's edge, stop.
                } else {
                    right = false;
                }
            }
            // Calculates how far we can go up. Other pieces and the board's edge are barriers.
            if (up) {
                int upIteration = highlightedIndex - (8 * i);
                // Stay within the edges of the board.
                if (upIteration > -1) {
                    // If there's no piece, a rook can go there and keep going.
                    if (board.getPiece(upIteration) == null) {
                        threatRange.add(upIteration);
                        // If there's an enemy piece, a rook can go there but no further.
                    } else if (board.getTeam(upIteration) != board.getTeam(highlightedIndex)) {
                        threatRange.add(upIteration);
                        up = false;
                        // Otherwise we're done.
                    } else {
                        up = false;
                    }
                    // If we're past the board's edge, stop.
                } else {
                    up = false;
                }
            }
            // Calculates how far we can go down. Other pieces and the board's edge are barriers
            if (down) {
                int downIteration = highlightedIndex + (8 * i);
                // Stay within the edges of the board.
                if(downIteration < 64) {
                    // If there's no piece, a rook can go there and keep going.
                    if (board.getPiece(downIteration) == null) {
                        threatRange.add(downIteration);
                        // If there's an enemy piece, a rook can go there but no further.
                    } else if (board.getTeam(downIteration) != board.getTeam(highlightedIndex)) {
                        threatRange.add(downIteration);
                        down = false;
                        // Otherwise we're done.
                    } else {
                        down = false;
                    }
                    // If we're past the board's edge, stop.
                } else {
                    down = false;
                }
            }
        }
    }

}
