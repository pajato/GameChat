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

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.exp.Checkerboard;
import com.pajato.android.gamechat.exp.Engine;
import com.pajato.android.gamechat.exp.ExpHelper;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.NotificationManager;
import com.pajato.android.gamechat.exp.Piece;
import com.pajato.android.gamechat.exp.Team;
import com.pajato.android.gamechat.exp.TileClickHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static com.pajato.android.gamechat.exp.State.active;
import static com.pajato.android.gamechat.exp.State.check;
import static com.pajato.android.gamechat.exp.State.primary_wins;
import static com.pajato.android.gamechat.exp.State.secondary_wins;
import static com.pajato.android.gamechat.exp.Team.PRIMARY;
import static com.pajato.android.gamechat.exp.Team.SECONDARY;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.KING;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.PAWN;
import static com.pajato.android.gamechat.exp.chess.ChessPiece.PieceType.ROOK;

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

public enum ChessEngine implements Engine {
    instance;

    // Private instance variables.

    /** The underlying UI board model class. */
    private Checkerboard mBoard;

    /** The experience model class. */
    private Chess mModel;

    // Public instance methods.

    /** Handle a move of the selected piece to the given position. */
    @Override public void handleMove(final int position) {
        // Determine if the clicked position is a capture.  If so, remove the piece at the position.
        boolean isCapture = mModel.board.hasPiece(position);
        if (isCapture)
            mModel.board.delete(position);

        // Deal with pawn promotion and castling.
        Team team = mModel.board.getSelectedPiece().getTeam();
        ChessPiece piece = mModel.board.getSelectedPiece();
        if (piece.isType(PAWN) && (position < 8 || position > 55))
            promotePawn(position, team);
        else
            mModel.board.add(position, mModel.board.getSelectedPiece());
        handleCastling(position);

        // Wrap up the move by ...
        int selectedPosition = mModel.board.getSelectedPosition();
        mModel.board.delete(selectedPosition);
        mModel.board.clearSelectedPiece();
        mModel.board.getPossibleMoves().clear();

        // Test to see if the move is a win or draw or puts the opposing King in check and update
        // the database.
        if (!checkFinished()) {
            team = mModel.turn ? SECONDARY : PRIMARY;
            mModel.setStateType(isInCheck(team) ? check : active);
        }
        mModel.toggleTurn();
        ExperienceManager.instance.updateExperience(mModel);
    }

    /** Establish the experience model (chess) and board for this handler. */
    @Override public void init(final Experience model, final Checkerboard board,
                               final TileClickHandler handler) {
        if (!(model instanceof Chess))
            return;
        mModel = (Chess) model;
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
        if (mModel.board.hasPiece(position)) {
            List<Integer> possibleMoves = getPossibleMoves(position);
            removeCheckExposingMoves(possibleMoves, mModel.board.getSelectedPosition());
            mModel.board.getPossibleMoves().addAll(possibleMoves);
        } else {
            mModel.board.clearSelectedPiece();
        }
        ExperienceManager.instance.updateExperience(mModel);
    }

    // Private instance methods.

    /** Check to see if the game is over or not by counting the kings on the board. */
    private boolean checkFinished() {
        // Generate win conditions. If one side runs out of pieces, the other side wins.
        Team team = mModel.turn ? SECONDARY : PRIMARY;
        if (team == PRIMARY && isInCheck(team) && getAllMoves(team).isEmpty()) {
            mModel.state = secondary_wins;
            mModel.setWinCount();
        } else if (team == SECONDARY && isInCheck(team) && getAllMoves(team).isEmpty()) {
            mModel.state = primary_wins;
            mModel.setWinCount();
        } else
            return false;

        // A side has won. Generate a suitable message.
        // TODO: is the following OK?
        if (mModel.state.isWin() || mModel.state.isTie()) {
            mModel.board.clearSelectedPiece();
            String doneMessage = getDoneMessage();
            BaseFragment fragment = ExpHelper.getBaseFragment(mModel);
            NotificationManager.instance.notifyGameDone(fragment, doneMessage);
            //mModel.setStateType(State.pending);
        }
        return true;
    }

    /** Returns a list containing the end position for all valid moves for the given team. */
    private List<Integer> getAllMoves(Team team) {
        List<Integer> allMoves = new ArrayList<>();

        // Deep copy the key set to allow our helper methods to modify the board while we iterate
        // through it.
        List<String> keys = new ArrayList<>();
        for (String key : mModel.board.getKeySet()) {
            keys.add(key);
        }

        // Get all valid moves. Note that this does not get moves that would put the user in check.
        for (String key : keys) {
            int position = mModel.board.getPosition(key);
            if (!mModel.board.getPiece(position).isTeam(team))
                continue;
            List<Integer> possibleMoves = getPossibleMoves(position);
            removeCheckExposingMoves(possibleMoves, position);
            for(Integer possibleMove : possibleMoves) {
                allMoves.add(possibleMove);
            }
        }
        return allMoves;
    }

    /** Return a done message text to show in a snackbar.  The given model provides the state. */
    private String getDoneMessage() {
        // Determine if there is a winner.  If not, return the "tie" message.
        BaseFragment fragment = ExpHelper.getBaseFragment(mModel);
        String name = mModel.getWinningPlayer().name;
        int resId = R.string.WinMessageNotificationFormat;
        String format = name != null ? "Checkmate! " + fragment.getString(resId): null;
        String message = format != null ? String.format(Locale.getDefault(), format, name) : null;
        return message != null ? message : fragment.getString(R.string.TieMessageNotification);
    }

    /** Returns a list of possible moves for a highlighted piece at a given position. */
    private List<Integer> getPossibleMoves(final int position) {
        // Validate the position.  Abort if is invalid, otherwise get the moves available to the
        // piece.
        List<Integer> result = new ArrayList<>();
        ChessPiece piece = mModel.board.getPiece(position);
        if (piece == null)
            return result;
        switch (mModel.board.getPiece(position).getPieceType()) {
            case PAWN:
                ChessHelper.getPawnThreatRange(result, position, mModel.board);
                break;
            case ROOK:
                ChessHelper.getRookThreatRange(result, position, mModel.board);
                break;
            case KNIGHT:
                ChessHelper.getKnightThreatRange(result, position, mModel.board);
                break;
            case BISHOP:
                ChessHelper.getBishopThreatRange(result, position, mModel.board);
                break;
            case QUEEN:
                ChessHelper.getQueenThreatRange(result, position, mModel.board);
                break;
            case KING:
                boolean[] castlingBooleans = {mModel.primaryQueenSideRookHasMoved,
                        mModel.primaryKingSideRookHasMoved, mModel.primaryKingHasMoved,
                        mModel.secondaryQueenSideRookHasMoved, mModel.secondaryKingSideRookHasMoved,
                        mModel.secondaryKingHasMoved};
                ChessHelper.getKingThreatRange(result, position, mModel.board, castlingBooleans);
                break;
        }
        return result;
    }

    /** Handle castling for the piece at the given position. */
    private void handleCastling(final int clickedPosition) {
        // Handle the movement of the Rook for Castling
        ChessPiece selectedPiece = mModel.board.getSelectedPiece();
        if (selectedPiece == null)
            return;

        // ...
        int selectedPosition = mModel.board.getSelectedPosition();
        boolean castlingKingSide = clickedPosition == selectedPosition + 2;
        boolean castlingQueenSide = clickedPosition == selectedPosition - 3;
        boolean isCastling = selectedPiece.isType(KING) && (castlingKingSide || castlingQueenSide);
        if (isCastling) {
            int rookPrevIndex;
            int rookFutureIndex;
            // Handle the side-dependent pieces of the castle (king-side vs queen-side)
            if (castlingKingSide) {
                rookPrevIndex = selectedPosition + 3;
                rookFutureIndex = selectedPosition + 1;
            } else {
                rookPrevIndex = selectedPosition - 4;
                rookFutureIndex = selectedPosition - 2;
            }

            // Put a rook at the new rook position.
            mModel.board.add(rookFutureIndex, ROOK, selectedPiece.getTeam());
            TextView futureRook = mBoard.getCell(rookFutureIndex);

            // Handle the player-dependent pieces of the castle (color)
            futureRook.setText(mModel.board.getPiece(rookFutureIndex).getText());
            futureRook.setTextColor(mModel.board.getTeam(rookFutureIndex).color);

            // Get rid of the old rook.
            TextView previousRook = mBoard.getCell(rookPrevIndex);
            previousRook.setText("");
            mModel.board.delete(rookPrevIndex);
        }

        // Handle the Castling Booleans.
        if (selectedPiece.isPiece(KING, PRIMARY))
            mModel.primaryKingHasMoved = true;
        else if (selectedPiece.isPiece(KING, SECONDARY))
            mModel.secondaryKingHasMoved = true;
        else if (selectedPiece.isPiece(ROOK, PRIMARY)) {
            if (selectedPosition == 0)
                mModel.primaryQueenSideRookHasMoved = true;
            else if (selectedPosition == 7)
                mModel.primaryKingSideRookHasMoved = true;
        } else if (selectedPiece.isPiece(ROOK, SECONDARY))
            if (selectedPosition == 56)
                mModel.secondaryQueenSideRookHasMoved = true;
            else if (selectedPosition == 63)
                mModel.secondaryKingSideRookHasMoved = true;
    }

    /** Return true iff the given team's King is in check. */
    private boolean isInCheck(final Team team) {
        // Determine if the given team's King is now in check.  Check all pieces on opposing team to
        // see if any of them threaten the King.  Get each player piece from the data model
        // filtering out the passive player's pieces.  Get the possible moves for each of these
        // pieces to see if the passive King can be captured.  If so return true.
        for (String key : mModel.board.getKeySet()) {
            int position = mModel.board.getPosition(key);
            if (mModel.board.getPiece(position).isTeam(team))
                continue;
            List<Integer> possibleMoves = getPossibleMoves(position);
            for (int possiblePosition : possibleMoves) {
                Piece piece = mModel.board.getPiece(possiblePosition);
                if (piece != null && piece.isPiece(KING, team))
                    return true;
            }
        }
        return false;
    }

    /** Handles the promotion of a pawn at the given position for the given team. */
    private void promotePawn(final int position, final Team team) {
        // Generate and show and alert dialog to inform the User about the promotion.
        Context context = ExpHelper.getBaseFragment(mModel).getContext();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        int resId = R.layout.pawn_dialog;
        alertDialogBuilder.setTitle(context.getString(R.string.PromotePawnMsg)).setView(resId);
        AlertDialog pawnChooser = alertDialogBuilder.create();
        pawnChooser.show();

        int color = team == PRIMARY ? ContextCompat.getColor(context, R.color.colorPrimary)
                : ContextCompat.getColor(context, R.color.colorAccent);

        // Change the Dialog's Icon color.
        int alertIconId = context.getResources().getIdentifier("android:id/icon", null, null);
        ImageView alertIcon = (ImageView) pawnChooser.findViewById(alertIconId);
        if (alertIcon != null) {
            alertIcon.setColorFilter(color, SRC_ATOP);
        }
        // Setup the Queen Listeners and change color appropriate to the team.
        TextView queenIcon = (TextView) pawnChooser.findViewById(R.id.queen_icon);
        TextView queenText = (TextView) pawnChooser.findViewById(R.id.queen_text);
        if (queenIcon != null && queenText != null) {
            queenIcon.setText(ChessPiece.PieceType.QUEEN.text);
            queenIcon.setTextColor(color);
            queenIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            queenText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // Do the same for bishop.
        TextView bishopIcon = (TextView) pawnChooser.findViewById(R.id.bishop_icon);
        TextView bishopText = (TextView) pawnChooser.findViewById(R.id.bishop_text);
        if (bishopIcon != null && bishopText != null) {
            bishopIcon.setText(ChessPiece.PieceType.BISHOP.text);
            bishopIcon.setTextColor(color);
            bishopIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            bishopText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // And the same for knight.
        TextView knightIcon = (TextView) pawnChooser.findViewById(R.id.knight_icon);
        TextView knightText = (TextView) pawnChooser.findViewById(R.id.knight_text);
        if (knightIcon != null && knightText != null) {
            knightIcon.setText(ChessPiece.PieceType.KNIGHT.text);
            knightIcon.setTextColor(color);
            knightIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            knightText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // And finally, the same for rook.
        TextView rookIcon = (TextView) pawnChooser.findViewById(R.id.rook_icon);
        TextView rookText = (TextView) pawnChooser.findViewById(R.id.rook_text);
        if (rookIcon != null && rookText != null) {
            rookIcon.setText(ROOK.text);
            rookIcon.setTextColor(color);
            rookIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            rookText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
    }

    /** Remove any possible moves that would result in placing the active King in check. */
    private void removeCheckExposingMoves(final List<Integer> possibleMoves, final int piecePosition) {
        // Collect the rejected moves for removal after all possible positions have been tested.
        List<Integer> exposedMoves = new ArrayList<>();
        for (int possiblePosition : possibleMoves)
            if (testForCheck(possiblePosition, piecePosition))
                exposedMoves.add(possiblePosition);
        for (int position : exposedMoves)
            possibleMoves.remove(Integer.valueOf(position));
    }

    /** Return true if moving the selected piece to the given position would expose a check. */
    private boolean testForCheck(final int position, final int selectedPosition) {
        // Move the selected piece to the possible move and test to see if the associated King
        // would be in check.  First, save any displaced piece information. Moving nothing cannot
        // put the player in check, so if there is no piece at the position, then return false.
        boolean result;
        ChessBoard board = mModel.board;
        ChessPiece savedSelectedPiece = board.delete(selectedPosition);
        if(savedSelectedPiece == null) return false;
        ChessPiece savedMovePiece = board.hasPiece(position) ? board.getPiece(position) : null;

        // Add the selected piece to the move position, clear it from the board and test for check.
        board.add(position, savedSelectedPiece);
        board.clearSelectedPiece();
        Team team = savedSelectedPiece.getTeam();
        result = isInCheck(team);

        // Restore the moved pieces.
        if (savedMovePiece != null)
            board.add(position, savedMovePiece);
        else
            board.delete(position);
        board.add(selectedPosition, savedSelectedPiece);
        board.setSelectedPosition(selectedPosition);
        return result;
    }

    // Private inner classes.

    private class Promoter implements View.OnClickListener {
        AlertDialog mDialog;
        int position;
        Team team;

        Promoter(final int indexClicked, final Team teamNumber, AlertDialog dialog) {
            position = indexClicked;
            team = teamNumber;
            mDialog = dialog;
        }

        @Override
        public void onClick(final View v) {
            int id = v.getId();
            ChessPiece.PieceType pieceType;
            switch (id) {
                default:
                case R.id.queen_icon:
                case R.id.queen_text:
                    pieceType = ChessPiece.PieceType.QUEEN;
                    break;
                case R.id.bishop_icon:
                case R.id.bishop_text:
                    pieceType = ChessPiece.PieceType.BISHOP;
                    break;
                case R.id.knight_icon:
                case R.id.knight_text:
                    pieceType = ChessPiece.PieceType.KNIGHT;
                    break;
                case R.id.rook_icon:
                case R.id.rook_text:
                    pieceType = ROOK;
                    break;
            }

            ChessBoard board = mModel.board;
            board.add(position, pieceType, team);
            TextView promotedPieceTile = mBoard.getCell(position);
            promotedPieceTile.setText(board.getPiece(position).getText());
            promotedPieceTile.setTextColor(board.getTeam(position).color);

            mDialog.dismiss();
        }
    }
}
