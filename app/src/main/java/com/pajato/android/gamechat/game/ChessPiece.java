package com.pajato.android.gamechat.game;

import android.util.SparseArray;

import com.pajato.android.gamechat.R;

import java.util.ArrayList;

/**
 * A simple P.O.J.O. class that keeps track of a chess pieces type and the team it is on.
 */
class ChessPiece {
    static final int KING = 0;
    static final int QUEEN = 1;
    static final int BISHOP = 2;
    static final int KNIGHT = 3;
    static final int ROOK = 4;
    static final int PAWN = 5;

    static final int BLUE_TEAM = 10;
    static final int OTHER_TEAM = 20;

    private int pieceId;
    private int teamId;

    ChessPiece(final int piece, final int team) {
        this.pieceId = piece;
        this.teamId = team;
    }

    int getPiece() {
        return this.pieceId;
    }

    int getTeam() {
        return this.teamId;
    }

    static int getDrawableFor(final int pieceType) {
        int drawable;
        switch(pieceType) {
            case KING: drawable = R.drawable.ic_stars_black_36dp;
                break;
            case QUEEN: drawable = R.drawable.ic_games_white;
                break;
            case BISHOP: drawable = R.drawable.ic_info_black;
                break;
            case KNIGHT: drawable = R.drawable.ic_help_black;
                break;
            case ROOK: drawable = R.drawable.ic_settings_black;
                break;
            default:
            case PAWN: drawable = R.drawable.ic_account_circle_black_36dp;
                break;
        }
        return drawable;
    }

    // Threat Range Methods

    /**
     * Gets the "threat range" of the King. A "threat range" is the full possible amount of
     * locations a piece can enter without breaking the rules of chess. The King's movement is
     * limited, and when the King of a side is captured, that side loses the game. A King can move
     * in any direction, but only a single square away. A King may move into the space of another
     * piece only if it is capturing that piece.
     *
     * @param threatRange the ArrayList that contains the threat range of the King.
     * @param highlightedIndex the index of the current piece.
     * @param boardMap a HashMap representing the board and the pieces located within.
     */
    static void getKingThreatRange(final ArrayList<Integer> threatRange, final int highlightedIndex,
                      final SparseArray<ChessPiece> boardMap, final boolean[] castlingBooleans) {
        final boolean blueQueenSideRookHasMoved = castlingBooleans[0];
        final boolean blueKingSideRookHasMoved = castlingBooleans[1];
        final boolean blueKingHasMoved = castlingBooleans[2];
        final boolean otherQueenSideRookHasMoved = castlingBooleans[3];
        final boolean otherKingSideRookHasMoved = castlingBooleans[4];
        final boolean otherKingHasMoved = castlingBooleans[5];

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
                if(boardMap.get(possibleMove, null) == null) {
                    threatRange.add(possibleMove);
                } else if (boardMap.get(possibleMove).getTeam()
                        != boardMap.get(highlightedIndex).getTeam()) {
                    threatRange.add(possibleMove);
                }
            }
        }

        // Handle Castling for Blue
        if(boardMap.get(highlightedIndex).getTeam() == ChessPiece.BLUE_TEAM && !blueKingHasMoved) {
            // The more common Castling variant, "Queen-Side Castling" or "Short Castling"
            boolean canCastleKingSide = highlightedIndex == 60 && !blueKingSideRookHasMoved &&
                    boardMap.get(highlightedIndex + 1, null) == null &&
                    boardMap.get(highlightedIndex + 2, null) == null &&
                    boardMap.get(highlightedIndex + 3, null) != null &&
                    boardMap.get(highlightedIndex + 3).getPiece() == ChessPiece.ROOK;
            if(canCastleKingSide) {
                threatRange.add(highlightedIndex + 2);
            }
            // The less common Castling variant, "Queen-Side Castling" or "Long Castling"
            boolean canCastleQueenSide = highlightedIndex == 60 && !blueQueenSideRookHasMoved &&
                    boardMap.get(highlightedIndex - 1, null) == null &&
                    boardMap.get(highlightedIndex - 2, null) == null &&
                    boardMap.get(highlightedIndex - 3, null) == null &&
                    boardMap.get(highlightedIndex - 4, null) != null &&
                    boardMap.get(highlightedIndex - 4).getPiece() == ChessPiece.ROOK;
            if(canCastleQueenSide) {
                threatRange.add(highlightedIndex - 3);
            }
        // Handle Castling for the other team
        } else if (boardMap.get(highlightedIndex).getTeam() == ChessPiece.OTHER_TEAM && !otherKingHasMoved) {
            boolean canCastleKingSide = highlightedIndex == 4 && !otherKingSideRookHasMoved &&
                    boardMap.get(highlightedIndex + 1, null) == null &&
                    boardMap.get(highlightedIndex + 2, null) == null &&
                    boardMap.get(highlightedIndex + 3, null) != null &&
                    boardMap.get(highlightedIndex + 3).getPiece() == ChessPiece.ROOK;
            if (canCastleKingSide) {
                threatRange.add(highlightedIndex + 2);
            }
            // The less common Castling variant, "Queen-Side Castling" or "Long Castling"
            boolean canCastleQueenSide = highlightedIndex == 4 && !otherQueenSideRookHasMoved &&
                    boardMap.get(highlightedIndex - 1, null) == null &&
                    boardMap.get(highlightedIndex - 2, null) == null &&
                    boardMap.get(highlightedIndex - 3, null) == null &&
                    boardMap.get(highlightedIndex - 4, null) != null &&
                    boardMap.get(highlightedIndex - 4).getPiece() == ChessPiece.ROOK;
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
     *
     * @param threatRange the ArrayList that contains the threat range of the Queen.
     * @param highlightedIndex the index of the current piece.
     * @param boardMap a HashMap representing the board and the pieces located within.
     */
    static void getQueenThreatRange(final ArrayList<Integer> threatRange, final int
            highlightedIndex, final SparseArray<ChessPiece> boardMap) {
        getRookThreatRange(threatRange, highlightedIndex, boardMap);
        getBishopThreatRange(threatRange, highlightedIndex, boardMap);
    }

    /**
     * Gets the "threat range" of the Bishop. A "threat range" is the full possible amount of
     * locations a piece can enter without breaking the rules of chess. Bishops can move across
     * the diagonals (up/right, down/right, down/left, and up/right), but cannot move past other
     * pieces. A bishop can take the place of another piece if they are capturing it.
     *
     * @param threatRange the ArrayList that contains the threat range of the Bishop.
     * @param highlightedIndex the index of the current piece.
     * @param boardMap a HashMap representing the board and the pieces located within.
     */
    static void getBishopThreatRange(final ArrayList<Integer> threatRange, final int
            highlightedIndex, final SparseArray<ChessPiece> boardMap) {
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
                    if (boardMap.get(upLeftIteration, null) == null) {
                        threatRange.add(upLeftIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (boardMap.get(upLeftIteration).getTeam() !=
                            boardMap.get(highlightedIndex).getTeam()) {
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
                    if (boardMap.get(upRightIteration, null) == null) {
                        threatRange.add(upRightIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (boardMap.get(upRightIteration).getTeam() !=
                            boardMap.get(highlightedIndex).getTeam()) {
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
                    if (boardMap.get(downRightIteration, null) == null) {
                        threatRange.add(downRightIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (boardMap.get(downRightIteration).getTeam() !=
                            boardMap.get(highlightedIndex).getTeam()) {
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
                    if (boardMap.get(downLeftIteration, null) == null) {
                        threatRange.add(downLeftIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (boardMap.get(downLeftIteration).getTeam() !=
                            boardMap.get(highlightedIndex).getTeam()) {
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
     *
     * @param threatRange the ArrayList that contains the threat range of the pawn.
     * @param highlightedIndex the index of the current piece.
     * @param boardMap a HashMap representing the board and the pieces located within.
     */
    static void getPawnThreatRange(final ArrayList<Integer> threatRange, final int
            highlightedIndex, final SparseArray<ChessPiece> boardMap) {
        // Pawns move differently depending on the team they are on.
        if(boardMap.get(highlightedIndex).getTeam() == ChessPiece.BLUE_TEAM) {
            int upLeft = highlightedIndex - 9;
            int upRight = highlightedIndex - 7;
            int up = highlightedIndex - 8;

            // Pawns can move two squares forward if they are in their starting position.
            if(highlightedIndex > 47 && highlightedIndex < 56) {
                int upUp = highlightedIndex -  16;
                if(boardMap.get(upUp, null) == null && boardMap.get(up, null) == null) {
                    threatRange.add(upUp);
                }
            }

            // Pawns can move diagonally forward only if they are capturing a piece.
            if(boardMap.get(upLeft, null) != null && upLeft % 8 != 7
                    && boardMap.get(upLeft).getTeam() == ChessPiece.OTHER_TEAM) {
                threatRange.add(upLeft);
            }
            if(boardMap.get(upRight, null) != null && upRight % 8 != 0
                    && boardMap.get(upRight).getTeam() == ChessPiece.OTHER_TEAM) {
                threatRange.add(upRight);
            }
            // Pawns can move forward only if they are not being blocked.
            if(boardMap.get(up, null) == null && up > -1) {
                threatRange.add(up);
            }
        } else {
            int downRight = highlightedIndex + 9;
            int downLeft = highlightedIndex + 7;
            int down = highlightedIndex + 8;

            // Pawns can move two squares forward if they are in their starting position.
            if(highlightedIndex > 7 && highlightedIndex < 16) {
                int downDown = highlightedIndex + 16;
                if(boardMap.get(downDown, null) == null && boardMap.get(down, null) == null) {
                    threatRange.add(downDown);
                }
            }

            // Pawns can move diagonally forward only if they are capturing a piece.
            if(boardMap.get(downRight, null) != null && downRight % 8 != 0
                    && boardMap.get(downRight).getTeam() == ChessPiece.BLUE_TEAM) {
                threatRange.add(downRight);
            }
            if(boardMap.get(downLeft, null) != null && downRight % 8 != 7
                    && boardMap.get(downLeft).getTeam() == ChessPiece.BLUE_TEAM) {
                threatRange.add(downLeft);
            }
            // Pawns can move forward only if they are not being blocked.
            if(boardMap.get(down, null) == null && down < 64) {
                threatRange.add(down);
            }
        }
    }

    /**
     * Gets the "threat range" of the Knight. A "threat range" is the full possible amount of
     * locations a piece can enter without breaking the rules of chess. Knights must move in an
     * L shape: two squares in one direction and one in another. They can jump other pieces.
     *
     * @param threatRange the ArrayList that contains the threat range of the knight piece.
     * @param highlightedIndex the index of the current piece.
     * @param boardMap a HashMap representing the board and the pieces located within.
     */
    static void getKnightThreatRange(final ArrayList<Integer> threatRange, final int
            highlightedIndex, final SparseArray<ChessPiece> boardMap) {
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
                if (boardMap.get(possibleMove, null) == null) {
                    threatRange.add(possibleMove);
                } else if (boardMap.get(possibleMove).getTeam() != boardMap.get(highlightedIndex).getTeam()) {
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
     * @param boardMap a HashMap representing the board and the pieces located within.
     */
    static void getRookThreatRange(final ArrayList<Integer> threatRange, final int
            highlightedIndex, final SparseArray<ChessPiece> boardMap) {
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
                    if (boardMap.get(leftIteration, null) == null) {
                        threatRange.add(leftIteration);
                        // If there's an enemy piece, we can go there but no further.
                    } else if (boardMap.get(leftIteration).getTeam() !=
                            boardMap.get(highlightedIndex).getTeam()) {
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
                    if(boardMap.get(rightIteration, null) == null) {
                        threatRange.add(rightIteration);
                        // If there's an enemy piece, a rook can go there but no further.
                    } else if (boardMap.get(rightIteration).getTeam() !=
                            boardMap.get(highlightedIndex).getTeam()) {
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
                    if (boardMap.get(upIteration, null) == null) {
                        threatRange.add(upIteration);
                        // If there's an enemy piece, a rook can go there but no further.
                    } else if (boardMap.get(upIteration).getTeam()
                            != boardMap.get(highlightedIndex).getTeam()) {
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
                    if (boardMap.get(downIteration, null) == null) {
                        threatRange.add(downIteration);
                        // If there's an enemy piece, a rook can go there but no further.
                    } else if (boardMap.get(downIteration).getTeam()
                            != boardMap.get(highlightedIndex).getTeam()) {
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