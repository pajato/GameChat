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

import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.exp.Team;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.BISHOP;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.KING;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.KNIGHT;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.NONE;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.PAWN;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.QUEEN;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.ROOK;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ChessBoardUnitTest {
    private static final String CELL_ID = "cell";
    // CELL_ID + String.valueOf(int position)

    /** Tests the positions of the board after the init method runs. */
    @Test public void testChessBoardInit() {
        ChessBoard board = new ChessBoard();
        board.init();

        for(int i = 0; i < 64; i++) {
            // Get the current piece's type and team
            ChessPiece.PieceType type = board.getPieceType(i);
            Team team = board.getTeam(i);

            // Assert team based on starting positions of the chess pieces.
            if (i >= 0 && i < 16) {
                Assert.assertEquals(team, Team.SECONDARY);
            } else if (i >= 48 && i < 65) {
                Assert.assertEquals(team, Team.PRIMARY);
            } else {
                Assert.assertEquals(team, Team.NONE);
            }
            // Assert PieceType based on starting positions of the chess pieces.
            switch (i) {
                default:
                    Assert.assertEquals(type, NONE);
                    break;
                case 0:case 7:
                case 56:case 63:
                    Assert.assertEquals(type, ROOK);
                    break;
                case 1:case 6:
                case 57:case 62:
                    Assert.assertEquals(type, KNIGHT);
                    break;
                case 2:case 5:
                case 58:case 61:
                    Assert.assertEquals(type, BISHOP);
                    break;
                case 3:
                case 59:
                    Assert.assertEquals(type, QUEEN);
                    break;
                case 4:
                case 60:
                    Assert.assertEquals(type, KING);
                    break;
                case 8:case 9:case 10:case 11:case 12:case 13:case 14:case 15:
                case 48:case 49:case 50:case 51:case 52:case 53:case 54:case 55:
                    Assert.assertEquals(type, ChessPiece.PieceType.PAWN);
                    break;
            }
        }
    }

    /** Test the various methods that insert or delete pieces from the board. */
    @Test public void testInsertionDeletion() {
        ChessBoard board = new ChessBoard();

        // Setup a Piece Map through a non-init process.
        final Map<String, ChessPiece> pieceMap = new HashMap<>();
        pieceMap.put(CELL_ID + String.valueOf(0), new ChessPiece(KING, Team.PRIMARY));
        pieceMap.put(CELL_ID + String.valueOf(63), new ChessPiece(KING, Team.SECONDARY));

        board.setPieces(pieceMap);
        board.add(8, new ChessPiece(PAWN, Team.PRIMARY));
        board.add(9, PAWN, Team.SECONDARY);

        // Check insertion and deletion methods
        Assert.assertTrue(board.hasPiece(0));
        Assert.assertTrue(board.hasPiece(8));
        Assert.assertTrue(board.hasPiece(9));
        Assert.assertFalse(board.hasPiece(1));

        board.delete(8);
        Assert.assertFalse(board.hasPiece(8));

        pieceMap.put(CELL_ID + String.valueOf(9), new ChessPiece(PAWN, Team.SECONDARY));
        Assert.assertEquals(pieceMap, board.getPieces());

    }

    /** Test the various methods that effect the selected position object. */
    @Test public void testSelectedPositions() {
        ChessBoard board = new ChessBoard();
        board.add(0, KING, Team.PRIMARY);
        board.add(9, PAWN, Team.SECONDARY);

        // Test the getter and setter for selected position
        Assert.assertFalse(board.hasSelectedPiece());
        board.setSelectedPosition(9);
        Assert.assertTrue(board.hasSelectedPiece());
        Assert.assertEquals(board.getSelectedPosition(), 9);

        // Test the selected piece getter gets the correct piece
        ChessPiece selected = board.getSelectedPiece();
        Assert.assertEquals(selected.getTeam(), Team.SECONDARY);
        Assert.assertEquals(selected.getPieceType(), PAWN);

        // Test behavior when the selected piece has been cleared.
        board.clearSelectedPiece();
        Assert.assertEquals(board.getSelectedPosition(), -1);
        Assert.assertNull(board.getSelectedPiece());
        Assert.assertFalse(board.hasSelectedPiece());
    }

    /** Test the various methods that effect the list of possible positions. */
    @Test public void testPossiblePositionMethods() {
        ChessBoard board = new ChessBoard();

        // Structure a very simple piece map and give it to the board.
        final Map<String, ChessPiece> pieceMap = new HashMap<>();
        pieceMap.put(CELL_ID + String.valueOf(0), new ChessPiece(PAWN, Team.PRIMARY));
        pieceMap.put(CELL_ID + String.valueOf(9), new ChessPiece(PAWN, Team.SECONDARY));
        board.setPieces(pieceMap);

        // Setup a small list of possible moves.
        board.setPossibleMoves(null);
        List<Integer> emptyPossibleMoves = board.getPossibleMoves();
        Assert.assertTrue(emptyPossibleMoves.isEmpty());

        List<Integer> filledPossibleMoves = new ArrayList<>();
        filledPossibleMoves.addAll(Arrays.asList(0, 1));

        // Check possible position methods.
        Assert.assertFalse(board.isHighlighted(0));

        board.setPossibleMoves(filledPossibleMoves);
        Assert.assertTrue(board.isHighlighted(0));
        Assert.assertTrue(board.isHighlighted(1));
        Assert.assertFalse(board.isHighlighted(8));
        Assert.assertFalse(board.isHighlighted(9));
        Assert.assertEquals(filledPossibleMoves, board.getPossibleMoves());
    }

    /** Check misc methods */
    @Test public void testMisc() {
        // Setup a simple board structure.
        ChessBoard board = new ChessBoard();
        ChessPiece piece = new ChessPiece(PAWN, Team.SECONDARY);
        board.add(0, piece);

        // Compare the board structure to a makeshift board map.
        final Map<String, ChessPiece> pieceMap = new HashMap<>();
        pieceMap.put(CELL_ID + String.valueOf(0), piece);

        Set<String> boardKeySet = board.getKeySet();
        Set<String> mapKeySet = pieceMap.keySet();
        Assert.assertEquals(mapKeySet, boardKeySet);

        // Check the conversion method from a sample key to the int associated with it
        Assert.assertEquals(0, board.getPosition(CELL_ID + String.valueOf(0)));
        Assert.assertEquals(-1, board.getPosition(CELL_ID + String.valueOf(CELL_ID)));
    }

}
