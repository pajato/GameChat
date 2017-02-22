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

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.exp.Checkerboard;
import com.pajato.android.gamechat.exp.Engine;
import com.pajato.android.gamechat.exp.ExpHelper;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.Team;
import com.pajato.android.gamechat.exp.TileClickHandler;
import com.pajato.android.gamechat.exp.model.Checkers;

import java.util.ArrayList;
import java.util.List;

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

    /** A flag remembering if possible moves are showing. */
    private boolean mIsShowingPossibleMoves = false;

    /** The experience model class. */
    private Checkers mModel;


    // Public constructors.

    // Public instance methods.

    /** Establish the experience model (chess) and board for this handler. */
    public void init(final Experience model, final Checkerboard board,
                     final TileClickHandler handler) {
        if (!(model instanceof Checkers))
            return;
        mModel = (Checkers) model;
        mBoard = board;
        handler.init(model);
    }

    /** Handle a click on the checkers board by ... */
    @Override public void processTileClick(final int position) {
        // Ensure that the experience model exists.  Abort if not, otherwise handle either a
        // piece selection or a piece move.
        if (mModel == null || mBoard == null)
            return;
        boolean changedBoard = false;
        if (mModel.board.hasSelectedPiece()) {
            changedBoard = showPossibleMoves(position);
            mModel.board.clearSelectedPiece();
        } else {
            if (mModel.board.hasPiece(position)) {
                mModel.board.setSelectedPosition(position);
                changedBoard = showPossibleMoves(position);
            }
        }
        if (changedBoard)
            ExperienceManager.instance.updateExperience(mModel);
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
    private void findJumpables(final int position, final int jumpablePosition,
                               final List<Integer> movementOptions) {
        // Create the boolean calculations for each of our conditions.
        boolean emptySpace = mModel.board.getPiece(jumpablePosition) == null;
        boolean breaksBorders = (position % 8 == 1 && jumpablePosition % 8 == 7)
                || (position % 8 == 6 && jumpablePosition % 8 == 0);
        boolean jumpsAlly;

        // Check if the piece being jumped is an ally piece.
        CheckersPiece highlightedPiece = mModel.board.getPiece(position);
        int jumpedIndex = (position + jumpablePosition) / 2;
        CheckersPiece jumpedPiece = mModel.board.getPiece(jumpedIndex);
        jumpsAlly = highlightedPiece.getTeam() == jumpedPiece.getTeam();
        if (emptySpace && !breaksBorders && !jumpsAlly)
            movementOptions.add(jumpablePosition);
    }

    /**
     * Locates the possible moves of the piece that is about to be highlighted.
     *
     * @param highlightedIndex the index containing the highlighted piece.
     */
    private List<Integer> getPossibleMoves(final int highlightedIndex) {
        List<Integer> result = new ArrayList<>();
        CheckersPiece highlightedPiece = mModel.board.getPiece(highlightedIndex);
        //String highlightedPieceType = board.get(String.valueOf(highlightedIndex));

        // Get the possible positions, post-move, for the piece.
        int upLeft = highlightedIndex - 9;
        int upRight = highlightedIndex - 7;
        int downLeft = highlightedIndex + 7;
        int downRight = highlightedIndex + 9;

        // Handle vertical edges of the board and non-king pieces.
        if (highlightedIndex / 8 == 0 || highlightedPiece.isPiece(PIECE, SECONDARY)) {
            upLeft = -1;
            upRight = -1;
        } else if (highlightedIndex / 8 == 7 || highlightedPiece.isPiece(PIECE, PRIMARY)) {
            downLeft = -1;
            downRight = -1;
        }

        // Handle horizontal edges of the board.
        if (highlightedIndex % 8 == 0) {
            upLeft = -1;
            downLeft = -1;
        } else if( highlightedIndex % 8 == 7) {
            upRight = -1;
            downRight = -1;
        }

        // Handle tiles that already contain other pieces. You can jump over enemy pieces,
        // but not allied pieces.
        if (mModel.board.getPiece(upLeft) != null) {
            findJumpables(highlightedIndex, upLeft - 9, result);
            upLeft = -1;
        }
        if (mModel.board.getPiece(upRight) != null) {
            findJumpables(highlightedIndex, upRight - 7, result);
            upRight = -1;
        }
        if (mModel.board.getPiece(downLeft) != null) {
            findJumpables(highlightedIndex, downLeft + 7, result);
            downLeft = -1;
        }
        if(mModel.board.getPiece(downRight) != null) {
            findJumpables(highlightedIndex, downRight + 9, result);
            downRight = -1;
        }

        // Put the values in our int array to return
        if (upLeft != -1)
            result.add(upLeft);
        if (upRight != -1)
            result.add(upRight);
        if (downLeft != -1)
            result.add(downLeft);
        if (downRight != -1)
            result.add(downRight);
        return result;
    }

    /** Handle the movement of the selected and highlighted positions. */
    private void handleMovement(final int positionClicked, final boolean isCapture) {
        // Check to see if our piece becomes a king piece and put its value into the board.
        CheckersPiece selectedPiece = mModel.board.getSelectedPiece();
        if (positionClicked < 8 && selectedPiece.isPiece(PIECE, PRIMARY))
            mModel.board.add(positionClicked, KING, PRIMARY);
        else if (positionClicked > 55 && selectedPiece.isPiece(PIECE, SECONDARY))
            mModel.board.add(positionClicked, KING, SECONDARY);
        else
            mModel.board.add(positionClicked, selectedPiece);

        // Set the text, typeface, and color the new cell.
        TextView cell = mBoard.getCell(positionClicked);
        CheckersPiece piece = mModel.board.getPiece(positionClicked);
        cell.setText(piece.getText());
        cell.setTypeface(null, piece.getTypeface());
        Context context = ExpHelper.getBaseFragment(mModel).getContext();
        int colorResId = mModel.turn ? R.color.colorPrimary : R.color.colorAccent;
        cell.setTextColor(ContextCompat.getColor(context, colorResId));

        // Handle capturing pieces.
        boolean finishedJumping = true;
        int selectedPosition = mModel.board.getSelectedPosition();
        if (isCapture) {
            int pieceCapturedIndex = (positionClicked + selectedPosition) / 2;
            mBoard.getCell(pieceCapturedIndex).setText("");
            if (mModel.board.hasPiece(pieceCapturedIndex))
                mModel.board.delete(pieceCapturedIndex);

            // If there are no more jumps, change turns. If there is at least one jump left, don't.
            List<Integer> possibleJumps = getPossibleMoves(positionClicked);
            for(int possiblePosition: possibleJumps) {
                if(possiblePosition != -1 && (possiblePosition > 9 + positionClicked
                        || (possiblePosition < positionClicked - 9))) {
                    finishedJumping = false;
                }
            }
        }

        mModel.board.delete(selectedPosition);
        mBoard.getCell(selectedPosition).setText("");
        ExpHelper.handleTurnChange(mModel, finishedJumping);
        checkFinished();
    }

    private boolean handleSelection(final int clickedPosition, List<Integer> possibleMoves) {
        // Set the background of the selected position
        boolean result = false;
        Context context = ExpHelper.getBaseFragment(mModel).getContext();
        int selectedPosition = mModel.board.getSelectedPosition();
        mBoard.handleTileBackground(context, selectedPosition);

        // Set the background of the possible moves, ignoring specially marked positions or
        // positions with no pieces on them.
        for (int possiblePosition : possibleMoves) {
            // If the tile clicked is one of the possible positions, and it's the correct
            // turn/piece combination, the piece moves there.
            if (clickedPosition == possiblePosition) {
                boolean isCapture = (clickedPosition > 9 + selectedPosition) ||
                        (clickedPosition < selectedPosition - 9);
                handleMovement(clickedPosition, isCapture);
                result = true;
            }
            mBoard.handleTileBackground(context, possiblePosition);
        }
        mModel.board.clearSelectedPiece();
        mBoard.getCell(selectedPosition).setText("");
        return result;
    }

    /** Return TRUE iff the board has changed as the result of a legal move. */
    private boolean showPossibleMoves(final int clickedPosition) {
        // If the game is over, we don't need to do anything.
        if (checkFinished()) {
            return false;
        }

        boolean result = false;
        int selectedPosition = mModel.board.getSelectedPosition();
        List<Integer> possibleMoves = getPossibleMoves(selectedPosition);

        // If a highlighted tile exists, we remove the highlight on it and its movement options.
        if (mIsShowingPossibleMoves)
            result = handleSelection(clickedPosition, possibleMoves);
        else {
            mModel.board.setSelectedPosition(clickedPosition);
            Context context = ExpHelper.getBaseFragment(mModel).getContext();
            mBoard.setHighlight(context, clickedPosition, possibleMoves);
        }

        mIsShowingPossibleMoves = !mIsShowingPossibleMoves;

        return result;
    }

    // Private inner classes.
}
