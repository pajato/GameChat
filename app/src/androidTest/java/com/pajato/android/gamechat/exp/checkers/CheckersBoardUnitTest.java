package com.pajato.android.gamechat.exp.checkers;

import com.pajato.android.gamechat.exp.Team;
import com.pajato.android.gamechat.exp.checkers.CheckersPiece.PieceType;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class CheckersBoardUnitTest {

    /** Ensure that the board is in the correct state when initialized. */
    @Test public void testInit() {
        CheckersBoard board = new CheckersBoard();
        board.init();

        for (int i = 0; i < 64; i++) {
            switch (i) {
                case 1:
                    assertEquals(board.getPieceType(i), PieceType.PIECE);
                    assertEquals(board.getTeam(i), Team.SECONDARY);
                case 3:case 5:case 7:case 8:case 10:case 12:case 14:
                case 17:case 19:case 21:case 23: {
                    assertTrue(board.hasPiece(i));
                    CheckersPiece piece = board.getPiece(i);
                    assertEquals(piece.getPieceType(), PieceType.PIECE);
                    assertEquals(piece.getTeam(), Team.SECONDARY);
                    break;
                }

                case 40:
                    assertEquals(board.getPieceType(i), PieceType.PIECE);
                    assertEquals(board.getTeam(i), Team.PRIMARY);
                case 42:case 44:case 46:case 49:case 51:case 53:case 55:
                case 56:case 58:case 60:case 62: {
                    assertTrue(board.hasPiece(i));
                    CheckersPiece piece = board.getPiece(i);
                    assertEquals(piece.getPieceType(), PieceType.PIECE);
                    assertEquals(piece.getTeam(), Team.PRIMARY);
                    break;
                }

                default:
                    assertFalse(board.hasPiece(i));
                    assertEquals(board.getPieceType(i), PieceType.NONE);
                    assertEquals(board.getTeam(i), Team.NONE);
                    break;
            }
        }
    }

    /** Ensure that we can add, delete, and query the board correctly with all relevant methods. */
    @Test public void testAddsAndDelete() {
        CheckersBoard board = new CheckersBoard();

        // Add two pieces to the board to setup board queries.
        board.add(1, PieceType.PIECE, Team.SECONDARY);
        board.add(42, new CheckersPiece(PieceType.KING, Team.PRIMARY));

        // Ensure piece 1's information is still intact.
        assertFalse(board.hasPiece(0));
        assertTrue(board.hasPiece(1));
        assertEquals(board.getPieceType(1), PieceType.PIECE);
        assertEquals(board.getTeam(1), Team.SECONDARY);

        // Ensure piece 2's information is still intact.
        CheckersPiece king = board.getPiece(42);
        assertFalse(board.hasPiece(41));
        assertTrue(board.hasPiece(42));
        assertEquals(king.getTeam(), Team.PRIMARY);
        assertEquals(king.getPieceType(), PieceType.KING);

        // Delete a piece and ensure it is deleted.
        board.delete(1);
        assertFalse(board.hasPiece(1));
        assertEquals(board.getPieceType(1), PieceType.NONE);
        assertEquals(board.getTeam(1), Team.NONE);

        // Add a piece through the map and ensure its information is still intact.
        Map<String, CheckersPiece> pieces = board.getPieces();
        assertEquals(1, pieces.size());
        pieces.put("cell" + String.valueOf(3), king);
        board.setPieces(pieces);
        assertTrue(board.hasPiece(3));
    }

    /** Ensure that we can select a specific piece on the board and query it correctly. */
    @Test public void testSelectedPiece() {
        CheckersBoard board = new CheckersBoard();
        assertFalse(board.hasSelectedPiece());

        // Set a piece and selected position.
        board.add(1, PieceType.PIECE, Team.SECONDARY);
        board.setSelectedPosition(1);
        assertTrue(board.hasSelectedPiece());
        assertEquals(1, board.getSelectedPosition());

        // Remove the selected position.
        CheckersPiece piece = board.getSelectedPiece();
        assertNotNull(piece);
        board.clearSelectedPiece();
        piece = board.getSelectedPiece();
        assertNull(piece);
    }

    /** Ensure that we can specify possible moves and query that information correctly. */
    @Test public void testPossibleMoves() {
        CheckersBoard board = new CheckersBoard();
        board.add(28, new CheckersPiece(PieceType.KING, Team.PRIMARY));

        List<Integer> possibleMoves = board.getPossibleMoves();
        assertEquals(0, possibleMoves.size());
        assertFalse(board.isHighlighted(19));
        possibleMoves.add(19);
        board.setPossibleMoves(possibleMoves);

        assertEquals(possibleMoves, board.getPossibleMoves());
        assertTrue(board.isHighlighted(19));
        assertFalse(board.isHighlighted(20));
    }

    /** Ensure that the formatting can be done correctly. */
    @Test public void testFormatter() {
        CheckersBoard board = new CheckersBoard();

        assertEquals(board.getPosition("cell"), -1);
        assertEquals(board.getPosition("cell" + String.valueOf(3)), 3);
    }

    /** Ensure that the board map's key set updates properly. */
    @Test public void testKeySet() {
        CheckersBoard board = new CheckersBoard();
        Set<String> keys = board.getKeySet();
        assertEquals(keys.size(), 0);

        board.add(0, PieceType.PIECE, Team.SECONDARY);
        keys = board.getKeySet();
        assertEquals(keys.size(), 1);
        assertTrue(keys.contains("cell" + String.valueOf(0)));
    }

}
