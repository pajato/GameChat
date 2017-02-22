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
import android.support.annotation.NonNull;
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
import com.pajato.android.gamechat.exp.Team;
import com.pajato.android.gamechat.exp.TileClickHandler;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;
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

    /** A flag remembering if possible moves are showing. */
    private boolean mIsShowingPossibleMoves = false;

    /** The experience model class. */
    private Chess mModel;

    // Public constructors.

    // Public instance methods.

    /** Establish the experience model (chess) and board for this handler. */
    public void init(final Experience model, final Checkerboard board,
                     final TileClickHandler handler) {
        if (!(model instanceof Chess))
            return;
        mModel = (Chess) model;
        mBoard = board;
        handler.init(model);
    }

    @Override public void processTileClick(final int position) {
        boolean changedBoard;
        if (mModel.board.hasSelectedPiece()) {
            changedBoard = showPossibleMoves(position);
            mModel.board.clearSelectedPiece();
        } else {
            if (mModel.board.hasPiece(position))
                mModel.board.setSelectedPosition(position);
                changedBoard = showPossibleMoves(position);
        }
        if (changedBoard)
            ExperienceManager.instance.updateExperience(mModel);
    }

    /** Return true iff the active player is in check. */
    @SuppressWarnings("unused")
    public boolean isInCheck(@NonNull final Chess model) {
        // Determine if any passive piece now threatens the active king.
        // TODO: implement...
        return false;
    }

    // Private instance methods.

    /** Check to see if the game is over or not by counting the kings on the board. */
    private boolean checkFinished() {
        // Generate win conditions. If one side runs out of pieces, the other side wins.
        String message;
        if (!mModel.board.containsSecondaryKing()) {
            message = "Game Over! Player 1 Wins!";
            mModel.state = primary_wins;
            mModel.setWinCount();
        } else if (!mModel.board.containsPrimaryKing()) {
            message = "Game Over! Player 2 Wins!";
            mModel.state = secondary_wins;
            mModel.setWinCount();
        } else
            return false;

        // A side has won. Generate a suitable message.
        BaseFragment fragment = ExpHelper.getBaseFragment(mModel);
        NotificationManager.instance.notifyGameDone(fragment, message);
        return true;
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


    /** Handles the movement of the pieces. */
    private void handleMovement(final int indexClicked, final boolean capturesPiece) {
        // Handle capturing pieces clearing the cell and removing the piece from the data model.
        mBoard.getCell(indexClicked).setText("");
        if (capturesPiece)
            mModel.board.delete(indexClicked);

        // Check to see if our pawn can becomes another piece and put its value into the board map.
        if (indexClicked < 8 && mModel.board.getSelectedPiece().isPiece(PAWN, PRIMARY)) {
            promotePawn(indexClicked, PRIMARY);
        } else if (indexClicked > 55 && mModel.board.getSelectedPiece().isPiece(PAWN, SECONDARY)) {
            promotePawn(indexClicked, SECONDARY);
        } else {
            // Add the clicked piece to the new position, set both the text and color values using
            // the source values.
            mModel.board.add(indexClicked, mModel.board.getSelectedPiece());
            TextView view = mBoard.getCell(indexClicked);
            view.setText(mModel.board.getPiece(indexClicked).getText());
            view.setTextColor(mModel.board.getTeam(indexClicked).color);
        }

        // Handle castling ....
        handleCastling(indexClicked);

        // Delete the piece's previous location and end the turn.
        int position = mModel.board.getSelectedPosition();
        mModel.board.delete(position);
        ExpHelper.handleTurnChange(mModel, true);
        checkFinished();
    }

    /** Return TRUE iff the given clicked position is a possible move for the selected position. */
    private boolean handleSelection(final int clickedPosition, final List<Integer> possibleMoves) {
        // Set the background of the selected position
        boolean result = false;
        Context context = ExpHelper.getBaseFragment(mModel).getContext();
        int selectedPosition = mModel.board.getSelectedPosition();
        mBoard.handleTileBackground(context, selectedPosition);

        // Set the background of the possible moves.
        for (int possiblePosition : possibleMoves) {
            // If the tile clicked is one of the possible positions, and it's the correct
            // turn/piece combination, the piece moves there.
            if (clickedPosition == possiblePosition) {
                boolean isCapture = mModel.board.containsPiece(clickedPosition);
                handleMovement(clickedPosition, isCapture);
                result = true;
            }
            mBoard.handleTileBackground(context, possiblePosition);
        }
        mModel.board.clearSelectedPiece();
        return result;
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

    /** Return TRUE iff the board has changed as the result of a legal move. */
    private boolean showPossibleMoves(final int clickedPosition) {
        // If the game is over, we don't need to do anything, so return.  Otherwise find the
        // possible moves for the selected piece.
        if (checkFinished())
            return false;
        boolean result = false;
        List<Integer> possibleMoves = getPossibleMoves(mModel.board.getSelectedPosition());

        // If the possible moves are showing, then remove the highlight from them as well as the
        // selected position, otherwise mark the clicked position as selected and highlight it
        // and the possible moves accordingly.
        if (mIsShowingPossibleMoves)
            result = handleSelection(clickedPosition, possibleMoves);
        else {
            mModel.board.setSelectedPosition(clickedPosition);
            Context context = ExpHelper.getBaseFragment(mModel).getContext();
            mBoard.setHighlight(context, clickedPosition, possibleMoves);
        }

        // Toggle the state of the "is showing possible moves" flag and return the result.
        mIsShowingPossibleMoves = !mIsShowingPossibleMoves;
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
