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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.BISHOP;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.KING;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.KNIGHT;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.PAWN;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.QUEEN;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.ROOK;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ChessHelperUnitTest {
    private static final String CELL_ID = "cell";

    /** */
    @Test public void testBishopThreatRange() {
        ChessBoard board = new ChessBoard();
        List<Integer> threatRange = new ArrayList<>();

        board.add(27, BISHOP, Team.PRIMARY);
        assertBishopThreatRange(threatRange, board, 27, Arrays.asList(0, 6, 9, 13, 18, 20, 34, 36,
                41, 45, 48, 54, 63));

        // Trying to get 100% coverage
        board.add(18, BISHOP, Team.SECONDARY);
        board.add(20, BISHOP, Team.SECONDARY);
        board.add(34, BISHOP, Team.SECONDARY);
        board.add(36, BISHOP, Team.SECONDARY);
        assertBishopThreatRange(threatRange, board, 27, Arrays.asList(18, 20, 34, 36));

        board.add(26, BISHOP, Team.PRIMARY);
        board.add(17, BISHOP, Team.PRIMARY);
        board.add(19, BISHOP, Team.PRIMARY);
        board.add(33, BISHOP, Team.PRIMARY);
        board.add(35, BISHOP, Team.PRIMARY);
        assertBishopThreatRange(threatRange, board, 26, Collections.<Integer>emptyList());
    }

    /** */
    @Test public void testKingThreatRange() {
        // Setup the required objects for testing
        ChessBoard board = new ChessBoard();
        List<Integer> threatRange = new ArrayList<>();
        Map<String, ChessPiece> pieceMap = new HashMap<>();
        board.setPieces(pieceMap);
        boolean[] falses = {false, false, false, false, false, false};
        boolean[] trues = {true, true, true, true, true, true};

        // Test the castling ranges and castling booleans for one team
        board.add(56, ROOK, Team.PRIMARY);
        board.add(63, ROOK, Team.PRIMARY);
        board.add(60, KING, Team.PRIMARY);
        assertKingThreatRange(threatRange, board, falses, 60, Arrays.asList(51, 52, 53, 57, 59, 61, 62));
        assertKingThreatRange(threatRange, board, trues, 60, Arrays.asList(51, 52, 53, 59, 61));

        // Test the same for the other team
        board.add(0, ROOK, Team.SECONDARY);
        board.add(7, ROOK, Team.SECONDARY);
        board.add(4, KING, Team.SECONDARY);
        assertKingThreatRange(threatRange, board, falses, 4, Arrays.asList(1, 3, 5, 6, 11, 12, 13));
        assertKingThreatRange(threatRange, board, trues, 4, Arrays.asList(3, 5, 11, 12, 13));

        // Test how kings interact with other pieces and edges of the board.
        pieceMap.clear();
        board.add(56, KING, Team.SECONDARY);
        board.add(48, PAWN, Team.SECONDARY);
        board.add(57, PAWN, Team.PRIMARY);
        assertKingThreatRange(threatRange, board, falses, 56, Arrays.asList(49, 57));

        board.add(63, KING, Team.PRIMARY);
        board.add(55, PAWN, Team.PRIMARY);
        board.add(62, PAWN, Team.SECONDARY);
        assertKingThreatRange(threatRange, board, falses, 63, Arrays.asList(54, 62));
    }

    /** */
    @Test public void testKnightThreatRange() {
        ChessBoard board = new ChessBoard();
        List<Integer> threatRange = new ArrayList<>();

        // Basic Tests
        board.add(27, KNIGHT, Team.PRIMARY);
        assertKnightThreatRange(threatRange, board, 27, Arrays.asList(10, 12, 17, 21, 33, 37, 42, 44));

        board.add(10, KNIGHT, Team.SECONDARY);
        board.add(12, KNIGHT, Team.PRIMARY);
        assertKnightThreatRange(threatRange, board, 27, Arrays.asList(10, 17, 21, 33, 37, 42, 44));

        // Trying to get 100% Missed Branches
        board.add(0, KNIGHT, Team.PRIMARY);
        assertKnightThreatRange(threatRange, board, 0, Arrays.asList(10, 17));
        board.add(63, KNIGHT, Team.PRIMARY);
        assertKnightThreatRange(threatRange, board, 63, Arrays.asList(46, 53));
        board.add(56, KNIGHT, Team.PRIMARY);
        assertKnightThreatRange(threatRange, board, 56, Arrays.asList(41, 50));
        board.add(57, KNIGHT, Team.PRIMARY);
        assertKnightThreatRange(threatRange, board, 57, Arrays.asList(40, 42, 51));
        board.add(62, KNIGHT, Team.PRIMARY);
        assertKnightThreatRange(threatRange, board, 62, Arrays.asList(45, 47, 52));
    }

    /** */
    @Test public void testPawnThreatRange() {
        ChessBoard board = new ChessBoard();
        List<Integer> threatRange = new ArrayList<>();
        Map<String, ChessPiece> pieceMap = new HashMap<>();
        board.setPieces(pieceMap);

        // Setup the board to test pawns breaking the edges of the map when capturing
        pieceMap.put(CELL_ID + String.valueOf(8), new ChessPiece(PAWN, Team.SECONDARY));
        pieceMap.put(CELL_ID + String.valueOf(15), new ChessPiece(PAWN, Team.PRIMARY));

        pieceMap.put(CELL_ID + String.valueOf(48), new ChessPiece(PAWN, Team.PRIMARY));
        pieceMap.put(CELL_ID + String.valueOf(39), new ChessPiece(PAWN, Team.SECONDARY));

        // Test that pawns don't break the edges of the board when capturing
        assertPawnThreatRange(threatRange, board, 15, Collections.singletonList(7));
        assertPawnThreatRange(threatRange, board, 8, Arrays.asList(16, 24));

        assertPawnThreatRange(threatRange, board, 48, Arrays.asList(32, 40));
        assertPawnThreatRange(threatRange, board, 39, Collections.singletonList(47));

        // Test that pawns can move diagonally to capture enemy pieces.
        pieceMap.clear();
        pieceMap.put(CELL_ID + String.valueOf(17), new ChessPiece(PAWN, Team.SECONDARY));
        pieceMap.put(CELL_ID + String.valueOf(24), new ChessPiece(PAWN, Team.PRIMARY));
        pieceMap.put(CELL_ID + String.valueOf(26), new ChessPiece(PAWN, Team.PRIMARY));

        assertPawnThreatRange(threatRange, board, 17, Arrays.asList(24, 25, 26));
        assertPawnThreatRange(threatRange, board, 24, Arrays.asList(16, 17));
        assertPawnThreatRange(threatRange, board, 26, Arrays.asList(17, 18));

        // Test that pawns cannot move diagonally into allied pieces
        pieceMap.put(CELL_ID + String.valueOf(33), new ChessPiece(PAWN, Team.PRIMARY));
        pieceMap.put(CELL_ID + String.valueOf(10), new ChessPiece(PAWN, Team.SECONDARY));
        pieceMap.put(CELL_ID + String.valueOf(19), new ChessPiece(PAWN, Team.SECONDARY));

        assertPawnThreatRange(threatRange, board, 33, Collections.singletonList(25));
        assertPawnThreatRange(threatRange, board, 10, Collections.singletonList(18));

        // Test pawn blockages for single movement forward.
        pieceMap.clear();
        pieceMap.put(CELL_ID + String.valueOf(8), new ChessPiece(PAWN, Team.SECONDARY));
        pieceMap.put(CELL_ID + String.valueOf(16), new ChessPiece(PAWN, Team.SECONDARY));

        pieceMap.put(CELL_ID + String.valueOf(40), new ChessPiece(PAWN, Team.PRIMARY));
        pieceMap.put(CELL_ID + String.valueOf(48), new ChessPiece(PAWN, Team.PRIMARY));

        assertPawnThreatRange(threatRange, board, 8, Collections.<Integer>emptyList());
        assertPawnThreatRange(threatRange, board, 48, Collections.<Integer>emptyList());

        // Test pawn blockages for double movement forward.
        pieceMap.remove(CELL_ID + String.valueOf(16));
        pieceMap.remove(CELL_ID + String.valueOf(40));
        pieceMap.put(CELL_ID + String.valueOf(24), new ChessPiece(PAWN, Team.SECONDARY));
        pieceMap.put(CELL_ID + String.valueOf(32), new ChessPiece(PAWN, Team.PRIMARY));

        assertPawnThreatRange(threatRange, board, 8, Collections.singletonList(16));
        assertPawnThreatRange(threatRange, board, 48, Collections.singletonList(40));
    }

    /** */
    @Test public void testQueenThreatRange() {
        ChessBoard board = new ChessBoard();
        List<Integer> threatRange = new ArrayList<>();

        // Test how Queens interact with the sides of the board.
        board.add(24, QUEEN, Team.PRIMARY);
        board.add(31, QUEEN, Team.SECONDARY);

        assertQueenThreatRange(threatRange, board, 24, Arrays.asList(0, 3, 8, 10, 16, 17, 25, 26,
                27, 28, 29, 30, 31, 32, 33, 40, 42, 48, 51, 56, 60));
        assertQueenThreatRange(threatRange, board, 31, Arrays.asList(4, 7, 13, 15, 22, 23, 24, 25,
                26, 27, 28, 29, 30, 38, 39, 45, 47, 52, 55, 59, 63));
    }

    /** */
    @Test public void testRookThreatRange() {
        ChessBoard board = new ChessBoard();
        List<Integer> threatRange = new ArrayList<>();

        board.add(27, ROOK, Team.PRIMARY);
        assertRookThreatRange(threatRange, board, 27, Arrays.asList(3, 11, 19, 24, 25, 26, 28, 29,
                30, 31, 35, 43, 51, 59));

        // Trying to get 100% coverage
        board.add(19, ROOK, Team.SECONDARY);
        board.add(26, ROOK, Team.SECONDARY);
        board.add(28, ROOK, Team.SECONDARY);
        board.add(35, ROOK, Team.SECONDARY);
        assertRookThreatRange(threatRange, board, 27, Arrays.asList(19, 26, 28, 35));

        board.add(17, ROOK, Team.SECONDARY);
        board.add(24, ROOK, Team.SECONDARY);
        board.add(25, ROOK, Team.SECONDARY);
        board.add(33, ROOK, Team.SECONDARY);
        assertRookThreatRange(threatRange, board, 25, Collections.<Integer>emptyList());
    }

    private void assertBishopThreatRange(final List<Integer> threat, final ChessBoard board,
                                       final int position, final List<Integer> expected) {
        threat.clear();
        ChessHelper.getBishopThreatRange(threat, position, board);
        assertThreatRange(threat, expected);
    }

    private void assertKingThreatRange(final List<Integer> threat, final ChessBoard board,
                                       final boolean[] castlingBooleans, final int position,
                                       final List<Integer> expected) {
        threat.clear();
        ChessHelper.getKingThreatRange(threat, position, board, castlingBooleans);
        assertThreatRange(threat, expected);
    }

    private void assertKnightThreatRange(final List<Integer> threat, final ChessBoard board,
                                         final int position, final List<Integer> expected) {
        threat.clear();
        ChessHelper.getKnightThreatRange(threat, position, board);
        assertThreatRange(threat, expected);
    }

    private void assertPawnThreatRange(final List<Integer> threat, final ChessBoard board,
                                       final int position, final List<Integer> expected) {
        threat.clear();
        ChessHelper.getPawnThreatRange(threat, position, board);
        assertThreatRange(threat, expected);
    }

    private void assertQueenThreatRange(final List<Integer> threat, final ChessBoard board,
                                        final int position, final List<Integer> expected) {
        threat.clear();
        ChessHelper.getQueenThreatRange(threat, position, board);
        assertThreatRange(threat, expected);
    }

    private void assertRookThreatRange(final List<Integer> threat, final ChessBoard board,
                                       final int position, final List<Integer> expected) {
        threat.clear();
        ChessHelper.getRookThreatRange(threat, position, board);
        assertThreatRange(threat, expected);
    }

    private void assertThreatRange(final List<Integer> threat, final List<Integer> expected) {
        Collections.sort(threat);
        Assert.assertEquals(expected, threat);
    }


}
