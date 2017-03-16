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

package com.pajato.android.gamechat.exp.checkers;

import com.pajato.android.gamechat.exp.Checkerboard;
import com.pajato.android.gamechat.exp.Engine;
import com.pajato.android.gamechat.exp.ExpHelper;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.Team;
import com.pajato.android.gamechat.exp.TileClickHandler;
import com.pajato.android.gamechat.exp.model.Checkers;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.exp.State.primary_wins;
import static com.pajato.android.gamechat.exp.State.secondary_wins;
import static com.pajato.android.gamechat.exp.Team.PRIMARY;
import static com.pajato.android.gamechat.exp.Team.SECONDARY;
import static com.pajato.android.gamechat.exp.checkers.CheckersPiece.PieceType.KING;
import static com.pajato.android.gamechat.exp.checkers.CheckersPiece.PieceType.PIECE;

/**
 * Provides a class to support playing chess alone, against another User and against the computer.
 *
 * Terminology:
 *
 * Active: refers to the player (or one of his/her pieces) whose turn it is to make a move.
 * Passive: refers to the player (or one of his/her pieces) who has just made a move.
 *
 * @author Paul Michael Reilly on Feb 14, 2017
 */

public enum CheckersEngine implements Engine {
    instance;

    // Private instance variables.

    /** The underlying UI board model class. */
    private Checkerboard mBoard;

    /** The experience model class. */
    private Checkers mModel;

    // Public instance methods.

    /** Handle a move of the selected piece to the given position. */
    @Override public void handleMove(final int position) {
        // Check to see if our piece becomes a king piece and put its value into the board.
        CheckersPiece selectedPiece = mModel.board.getSelectedPiece();
        if (position < 8 && selectedPiece.isPiece(PIECE, PRIMARY))
            mModel.board.add(position, KING, PRIMARY);
        else if (position > 55 && selectedPiece.isPiece(PIECE, SECONDARY))
            mModel.board.add(position, KING, SECONDARY);
        else
            mModel.board.add(position, selectedPiece);

        // Determine if the clicked position is a capture.  If so, remove the piece at the position.
        int selectedPosition = mModel.board.getSelectedPosition();
        boolean finishedJumping = true;
        if ((position > 9 + selectedPosition) || (position < selectedPosition - 9)) {
            int pieceCapturedIndex = (position + selectedPosition) / 2;
            mBoard.getCell(pieceCapturedIndex).setText("");
            if (mModel.board.hasPiece(pieceCapturedIndex))
                mModel.board.delete(pieceCapturedIndex);

            // If there are no more jumps, change turns. If there is at least one jump left, don't.
            List<Integer> possibleJumps = getPossibleMoves(position);
            for (int possiblePosition: possibleJumps) {
                if (possiblePosition != -1 && ((possiblePosition > 9 + position) ||
                                               (possiblePosition < position - 9))) {
                    finishedJumping = false;
                }
            }
        }

        // Test to see if jumping is finished.  If not, then start another move, otherwise change
        // turns, look for a finished state and update the database.
        mModel.board.clearSelectedPiece();
        mModel.board.getPossibleMoves().clear();
        mModel.board.delete(selectedPosition);
        if (!finishedJumping)
            startMove(position);
        else {
            mModel.toggleTurn();
            if (noMovesAvailable())
                mModel.setStateType(mModel.turn ? secondary_wins : primary_wins);
            checkFinished();
        }
        ExpHelper.updateModel(mModel);
    }

    /** Establish the experience model (chess) and board for this handler. */
    @Override public void init(final Experience model, final Checkerboard board,
                               final TileClickHandler handler) {
        if (!(model instanceof Checkers))
            return;
        mModel = (Checkers) model;
        mBoard = board;
        board.init(ExpHelper.getBaseFragment(model), handler);
        handler.setModel(model);
    }

    /** Start a move by marking the given position as the selected position and get a move list. */
    @Override public void startMove(final int position) {
        // Ignore clicks on invalid positions.  If the position represents a valid piece then
        // mark it as the selected piece and get a list of possible move positions that exclude
        // the possibility of putting the moving player into check.
        mModel.board.setSelectedPosition(position);
        mModel.board.getPossibleMoves().clear();
        if (mModel.board.hasPiece(position))
            mModel.board.getPossibleMoves().addAll(getPossibleMoves(position));
        else
            mModel.board.clearSelectedPiece();
        ExpHelper.updateModel(mModel);
    }

    // Private instance methods.

    /** Return TRUE iff the game is over because there is a winner. */
    private boolean checkFinished() {
        return hasNoPieces(PRIMARY) || hasNoPieces(SECONDARY);
    }

    /** Return TRUE iff the given team has no pieces on the board. */
    private boolean hasNoPieces(final Team team) {
        for (CheckersPiece piece : mModel.board.getPieces().values())
            if (piece.getTeam() == team)
                return false;
        return true;
    }

    /** Finds the "jumpable" pieces that the piece at the given position could capture. */
    private void findJumpables(final List<Integer> possibleMoves, final int position,
                               final int basePosition, final int offset) {
        // Ensure that the base position contains a piece and check to see if the piece being
        // jumped is an ally and is on the board.
        int jumpPosition = basePosition + offset;
        if (mModel.board.hasPiece(basePosition)) {
            boolean breaksBorders = jumpPosition < 0 || jumpPosition > 63
                    || (position % 8 == 1 && jumpPosition % 8 == 7)
                    || (position % 8 == 6 && jumpPosition % 8 == 0);
            CheckersPiece highlightedPiece = mModel.board.getPiece(position);
            int jumpedIndex = (position + jumpPosition) / 2;
            CheckersPiece jumpedPiece = mModel.board.getPiece(jumpedIndex);
            boolean jumpsAlly = highlightedPiece.getTeam() == jumpedPiece.getTeam();
            if (!mModel.board.hasPiece(jumpPosition) && !breaksBorders && !jumpsAlly)
                possibleMoves.add(jumpPosition);
        } else if (basePosition > -1 && basePosition < 64)
            possibleMoves.add(basePosition);
    }

    /**
     * Locates the possible moves of the piece that is about to be highlighted.
     *
     * @param position the index containing the highlighted piece.
     */
    private List<Integer> getPossibleMoves(final int position) {
        List<Integer> result = new ArrayList<>();
        CheckersPiece highlightedPiece = mModel.board.getPiece(position);
        //String highlightedPieceType = board.get(String.valueOf(position));

        // Get the possible positions, post-move, for the piece.
        int upLeft = position - 9;
        int upRight = position - 7;
        int downLeft = position + 7;
        int downRight = position + 9;

        // Handle vertical edges of the board and non-king pieces.
        if (position / 8 == 0 || highlightedPiece.isPiece(PIECE, SECONDARY)) {
            upLeft = -1;
            upRight = -1;
        } else if (position / 8 == 7 || highlightedPiece.isPiece(PIECE, PRIMARY)) {
            downLeft = -1;
            downRight = -1;
        }

        // Handle horizontal edges of the board.
        if (position % 8 == 0) {
            upLeft = -1;
            downLeft = -1;
        } else if( position % 8 == 7) {
            upRight = -1;
            downRight = -1;
        }

        // Handle tiles that already contain other pieces. You can jump over enemy pieces,
        // but not allied pieces.
        findJumpables(result, position, upLeft, -9);
        findJumpables(result, position, upRight, -7);
        findJumpables(result, position, downLeft, 7);
        findJumpables(result, position, downRight, 9);
        return result;
    }

    /** Test for the case where a play has been made and the other team has no moves. */
    private boolean noMovesAvailable() {
        // Establish the team being scrutinized and check for at least one move from all the players
        // on that team.
        Team team = mModel.turn ? PRIMARY : SECONDARY;
        for (String key : mModel.board.getKeySet()) {
            int position = mModel.board.getPosition(key);
            if (mModel.board.getTeam(position) == team && getPossibleMoves(position).size() != 0)
                return false;
        }
        return true;
    }
}
