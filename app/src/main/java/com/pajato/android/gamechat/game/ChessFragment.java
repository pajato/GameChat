package com.pajato.android.gamechat.game;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * A simple Chess game for use in GameChat.
 *
 * @author Bryan Scott
 */
public class ChessFragment extends BaseFragment {
    public boolean mTurn;

    // Board Management Objects
    private GridLayout mBoard;
    private SparseArray<ChessPiece> mBoardMap;
    private ImageButton mHighlightedTile;
    private boolean mIsHighlighted = false;
    private View mLayout;
    private ArrayList<Integer> mPossibleMoves;

    // Castle Management Objects
    private boolean mBlueQueenSidePawnHasMoved;
    private boolean mBlueKingSidePawnHasMoved;
    private boolean mBlueKingHasMoved;
    private boolean mOtherQueenSidePawnHasMoved;
    private boolean mOtherKingSidePawnHasMoved;
    private boolean mOtherKingHasMoved;


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_checkers, container, false);
        mBoard = (GridLayout) mLayout.findViewById(R.id.board);
        mTurn = false;
        onNewGame();

        ImageView playerOneIcon = (ImageView) mLayout.findViewById(R.id.player_1_icon);
        playerOneIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary),
                PorterDuff.Mode.SRC_ATOP);

        ImageView playerTwoIcon = (ImageView) mLayout.findViewById(R.id.player_2_icon);
        playerTwoIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent),
                PorterDuff.Mode.SRC_ATOP);

        return mLayout;
    }


    /**
     * A stand-in method that allows for creating a new game via game manager. Translates the
     * messages sent by the event system and handles the individual clicks based on messages sent.
     *
     * @param msg the message to be handled.
     */
    public void messageHandler(final String msg) {
        //TODO: Replace with the event bus system.
        Scanner input = new Scanner(msg);
        String player = input.nextLine();
        String buttonTag = input.nextLine();
        input.close();

        if(buttonTag.equals(getString(R.string.new_game))) {
            onNewGame();

            int color;
            if(player.equals("Purple")) {
                color = ContextCompat.getColor(getContext(), R.color.colorAccent);
                handleTurnChange();
            } else {
                player = "Blue";
                color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            }
            GameManager.instance.generateSnackbar(mLayout, getString(R.string.new_game) + " "
                    + player + "'s Turn!", color, false);
        }

    }

    /**
     * Handles a new game of checkers, resetting the board.
     */
    private void onNewGame() {
        mBoard.removeAllViews();
        mBoardMap = new SparseArray<>();
        mBoardMap.clear();
        mPossibleMoves = new ArrayList<>();
        mPossibleMoves.clear();

        // Reset the castling booleans.
        mBlueQueenSidePawnHasMoved = false;
        mBlueKingSidePawnHasMoved = false;
        mBlueKingHasMoved = false;
        mOtherQueenSidePawnHasMoved = false;
        mOtherKingSidePawnHasMoved = false;
        mOtherKingHasMoved = false;

        // Go through and populate the GridLayout / Board.
        for(int i = 0; i < 64; i++) {
            ImageButton currentTile = new ImageButton(getContext());

            // Set up the gridlayout params, so that each cell is functionally identical.
            //TODO: Handle alternate resolutions in a better way.
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = 90;
            param.width = 90;
            param.rightMargin = 0;
            param.topMargin = 0;
            param.setGravity(Gravity.CENTER);
            param.rowSpec = GridLayout.spec(i / 8);
            param.columnSpec = GridLayout.spec(i % 8);

            // Set up the Tile-specific information.
            currentTile.setLayoutParams(param);
            currentTile.setTag(i);
            handleTileBackground(i, currentTile);

            // Handle the starting piece positions.
            boolean containsNonBluePiece = i < 16;
            boolean containsBluePiece = i > 47;
            boolean containsPiece = containsBluePiece || containsNonBluePiece;
            int team;
            int color;

            // If the tile is meant to contain a board piece at the start of play, give it a piece.
            if(containsPiece) {
                if(containsBluePiece) {
                    team = ChessPiece.BLUE_TEAM;
                    color = R.color.colorPrimary;
                } else {
                    team = ChessPiece.OTHER_TEAM;
                    color = R.color.colorAccent;
                }
                switch(i) {
                    default:
                        mBoardMap.put(i, new ChessPiece(ChessPiece.PAWN, team));
                        currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.PAWN));
                        break;
                    case 0: case 7:
                    case 56: case 63:
                        mBoardMap.put(i, new ChessPiece(ChessPiece.ROOK, team));
                        currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.ROOK));
                        break;
                    case 1: case 6:
                    case 57: case 62:
                        mBoardMap.put(i, new ChessPiece(ChessPiece.KNIGHT, team));
                        currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.KNIGHT));
                        break;
                    case 2: case 5:
                    case 58: case 61:
                        mBoardMap.put(i, new ChessPiece(ChessPiece.BISHOP, team));
                        currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.BISHOP));
                        break;
                    case 3:
                    case 59:
                        mBoardMap.put(i, new ChessPiece(ChessPiece.QUEEN, team));
                        currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.QUEEN));
                        break;
                    case 4:
                    case 60:
                        mBoardMap.put(i, new ChessPiece(ChessPiece.KING, team));
                        currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.KING));
                }
                currentTile.setColorFilter(ContextCompat.getColor(getContext(), color), PorterDuff.Mode.SRC_ATOP);
            }
            currentTile.setOnClickListener(new ChessClick());
            mBoard.addView(currentTile);
        }

        handleTurnChange();
    }

    /**
     * showPossibleMoves handles highlighting possible movement options of a clicked piece,
     * then on a subsequent click it removes those highlights.
     *
     * @param indexClicked the index of the tile clicked.
     */
    private void showPossibleMoves(final int indexClicked) {
        // If the game is over, we don't need to do anything.
        if(checkFinished()) {
            return;
        }

        int highlightedIndex = (int) mHighlightedTile.getTag();
        findPossibleMoves(highlightedIndex, mPossibleMoves);

        // If a highlighted tile exists, we remove the highlight on it and its movement options.
        if(mIsHighlighted) {
            handleTileBackground(highlightedIndex, mHighlightedTile);

            for (int possiblePosition : mPossibleMoves) {
                // If the tile clicked is one of the possible positions, and it's the correct
                // turn/piece combination, the piece moves there.
                if(indexClicked == possiblePosition) {
                    boolean capturesPiece = (indexClicked > 9 + highlightedIndex) ||
                            (indexClicked < highlightedIndex - 9);
                    if(mTurn && (mBoardMap.get(highlightedIndex).getTeam() == ChessPiece.BLUE_TEAM)) {
                        handleMovement(true, indexClicked, capturesPiece);
                    } else if(!mTurn && (mBoardMap.get(highlightedIndex).getTeam() == ChessPiece.OTHER_TEAM)) {
                        handleMovement(false, indexClicked, capturesPiece);
                    }
                }
                handleTileBackground(possiblePosition, (ImageButton) mBoard.getChildAt(possiblePosition));
            }
            mHighlightedTile = null;

            // Otherwise, we need to highlight the tile clicked and its potential move squares with red.
        } else {
            mHighlightedTile.setBackgroundColor(ContextCompat.getColor(getContext(),
                    android.R.color.holo_red_dark));
            for(int possiblePosition : mPossibleMoves) {
                mBoard.getChildAt(possiblePosition).setBackgroundColor(ContextCompat
                        .getColor(getContext(), android.R.color.holo_red_light));
            }
        }

        mIsHighlighted = !mIsHighlighted;
    }

    /**
     * Checks to see if the game is over or not by counting the number of blue / yellow pieces on
     * the board. If there are zero of one type of pieces, then the other side wins.
     *
     * @return true if the game is over, false otherwise.
     */
    private boolean checkFinished() {
        // Generate win conditions. If one side runs out of pieces, the other side wins.
        int otherKing = 0;
        int blueKing = 0;

        for(int i = 0; i < 64; i++) {
            ChessPiece tmp = mBoardMap.get(i, null);
            if(tmp != null) {
                if(tmp.getPiece() == ChessPiece.KING) {
                    if(tmp.getTeam() == ChessPiece.BLUE_TEAM) {
                        blueKing++;
                    } else if (tmp.getTeam() == ChessPiece.OTHER_TEAM) {
                        otherKing++;
                    }
                }
            }
        }
        // Verify win conditions. If one passes, return true and generate an endgame snackbar.
        if(otherKing == 0) {
            GameManager.instance.generateSnackbar(mBoard, "Game Over! Blue Wins!",
                    ContextCompat.getColor(getContext(), R.color.colorPrimary), true);
            return true;
        } else if (blueKing == 0) {
            GameManager.instance.generateSnackbar(mBoard, "Game Over! Purple Wins!",
                    ContextCompat.getColor(getContext(), R.color.colorPrimaryDark), true);
            return true;
        } else {
            return false;
        }
    }

    private void handleTileBackground(final int index, final ImageButton currentTile) {
        // Handle the checkerboard positions.
        boolean isEven = (index % 2 == 0);
        boolean isOdd = (index % 2 == 1);
        boolean evenRowEvenColumn = ((index / 8) % 2 == 0) && isEven;
        boolean oddRowOddColumn = ((index / 8) % 2 == 1) && isOdd;

        // Create the checkerboard pattern on the button backgrounds.
        if(evenRowEvenColumn || oddRowOddColumn) {
            currentTile.setBackgroundColor(ContextCompat.getColor(
                    getContext(), android.R.color.white));
        } else {
            currentTile.setBackgroundColor(ContextCompat.getColor(
                    getContext(), android.R.color.darker_gray));
        }
    }

    /**
     * Locates the possible moves of the piece that is about to be highlighted.
     *
     * @param highlightedIndex the index containing the highlighted piece.
     */
    private void findPossibleMoves(final int highlightedIndex, final ArrayList<Integer> possibleMoves) {
        if(highlightedIndex < 0 || highlightedIndex > 64) {
            return;
        }

        possibleMoves.clear();
        int highlightedPieceType = mBoardMap.get(highlightedIndex).getPiece();

        switch(highlightedPieceType) {
            case ChessPiece.PAWN:
                ChessPiece.getPawnThreatRange(possibleMoves, highlightedIndex, mBoardMap);
                break;
            case ChessPiece.ROOK:
                ChessPiece.getRookThreatRange(possibleMoves, highlightedIndex, mBoardMap);
                break;
            case ChessPiece.KNIGHT:
                ChessPiece.getKnightThreatRange(possibleMoves, highlightedIndex, mBoardMap);
                break;
            case ChessPiece.BISHOP:
                ChessPiece.getBishopThreatRange(possibleMoves, highlightedIndex, mBoardMap);
                break;
            case ChessPiece.QUEEN:
                ChessPiece.getQueenThreatRange(possibleMoves, highlightedIndex, mBoardMap);
                break;
            case ChessPiece.KING:
                boolean[] castlingBooleans = {mBlueQueenSidePawnHasMoved, mBlueKingSidePawnHasMoved,
                        mBlueKingHasMoved, mOtherQueenSidePawnHasMoved, mOtherKingSidePawnHasMoved,
                        mOtherKingHasMoved};
                ChessPiece.getKingThreatRange(possibleMoves, highlightedIndex, mBoardMap,
                        castlingBooleans);
                break;
        }

    }

    /**
     * Handles the movement of the pieces.
     *
     * @param player indicates the current player. True is blue, False is yellow.
     * @param indexClicked the index of the clicked tile and the new position of the piece.
     */
    private void handleMovement(final boolean player, final int indexClicked,
                                final boolean capturesPiece) {
        // Reset the highlighted tile's image.
        mHighlightedTile.setImageResource(0);
        int highlightedIndex = (int) mHighlightedTile.getTag();
        ChessPiece highlightedPiece = mBoardMap.get(highlightedIndex);

        // Handle capturing pieces.
        if(capturesPiece) {
            ImageButton capturedTile = (ImageButton) mBoard.getChildAt(indexClicked);
            capturedTile.setImageResource(0);
            mBoardMap.delete(indexClicked);
        }

        // Check to see if our pawn can becomes another piece and put its value into the board map.
        if(indexClicked < 8 && highlightedPiece.getPiece() == ChessPiece.PAWN &&
                highlightedPiece.getTeam() == ChessPiece.BLUE_TEAM) {
            promotePawn(indexClicked, ChessPiece.BLUE_TEAM);
        } else if (indexClicked > 55 && highlightedPiece.getPiece() == ChessPiece.PAWN &&
                highlightedPiece.getTeam() == ChessPiece.OTHER_TEAM) {
            promotePawn(indexClicked, ChessPiece.OTHER_TEAM);
        } else {
            mBoardMap.put(indexClicked, highlightedPiece);

            // Find the new tile and give it a piece.
            ImageButton newLocation = (ImageButton) mBoard.getChildAt(indexClicked);
            newLocation.setImageResource(ChessPiece.getDrawableFor(mBoardMap.get(indexClicked).getPiece()));

            // Color the piece according to the player.
            int color;
            if(player) {
                color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            } else {
                color = ContextCompat.getColor(getContext(), R.color.colorAccent);
            }
            newLocation.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }

        // Handle the movement of the Rook for Castling
        boolean castlingKingSide = indexClicked == highlightedIndex + 2;
        boolean castlingQueenSide = indexClicked == highlightedIndex - 3;
        boolean isCastling = mBoardMap.get(highlightedIndex).getPiece() == ChessPiece.KING &&
                (castlingKingSide || castlingQueenSide);
        if(isCastling) {
            int color;
            int team;
            // Handle the player-dependent pieces of the castle.
            if(player) {
                color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
                team = ChessPiece.BLUE_TEAM;
            } else {
                color = ContextCompat.getColor(getContext(), R.color.colorAccent);
                team = ChessPiece.OTHER_TEAM;
            }
            int rookPrevIndex;
            int rookFutureIndex;
            // Handle the side-dependent pieces of the castle (king-side vs queen-side)
            if(castlingKingSide) {
                rookPrevIndex = highlightedIndex + 3;
                rookFutureIndex = highlightedIndex + 1;
            } else {
                rookPrevIndex = highlightedIndex - 4;
                rookFutureIndex = highlightedIndex - 2;
            }

            // Put a rook at the new rook position.
            mBoardMap.put(rookFutureIndex, new ChessPiece(ChessPiece.ROOK, team));
            ImageButton futureRook = (ImageButton) mBoard.getChildAt(rookFutureIndex);
            futureRook.setImageResource(ChessPiece.getDrawableFor(ChessPiece.ROOK));
            futureRook.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

            // Get rid of the old rook.
            ImageButton previousRook = (ImageButton) mBoard.getChildAt(rookPrevIndex);
            previousRook.setImageResource(0);
            mBoardMap.delete(rookPrevIndex);
        }

        // Handle the Castling Booleans.
        ChessPiece currentPiece = mBoardMap.get(highlightedIndex);
        if(currentPiece.getPiece() == ChessPiece.KING) {
            if(currentPiece.getTeam() == ChessPiece.BLUE_TEAM) {
                mBlueKingHasMoved = true;
            } else {
                mOtherKingHasMoved = true;
            }
        } else if (currentPiece.getPiece() == ChessPiece.ROOK) {
            if(currentPiece.getTeam() == ChessPiece.BLUE_TEAM) {
                if(highlightedIndex == 0) {
                    mBlueQueenSidePawnHasMoved = true;
                } else if (highlightedIndex == 7) {
                    mBlueKingSidePawnHasMoved = true;
                }
            } else {
                if(highlightedIndex == 56) {
                    mOtherQueenSidePawnHasMoved = true;
                } else if (highlightedIndex == 63) {
                    mOtherKingSidePawnHasMoved = true;
                }
            }
        }

        // Delete the piece's previous location and end the turn.
        mBoardMap.delete(highlightedIndex);
        handleTurnChange();
        checkFinished();
    }

    /**
     * Handles changing the turn and turn indicator.
     */
    private void handleTurnChange() {
        mTurn = !mTurn;

        // Handle the TextViews that serve as our turn indicator.
        TextView playerOneLeft = (TextView) mLayout.findViewById(R.id.player_1_left_indicator);
        TextView playerOneRight = (TextView) mLayout.findViewById(R.id.player_1_right_indicator);
        TextView playerTwoLeft = (TextView) mLayout.findViewById(R.id.player_2_left_indicator);
        TextView playerTwoRight = (TextView) mLayout.findViewById(R.id.player_2_right_indicator);

        if(mTurn) {
            playerOneLeft.setVisibility(View.VISIBLE);
            playerOneRight.setVisibility(View.VISIBLE);
            playerTwoLeft.setVisibility(View.INVISIBLE);
            playerTwoRight.setVisibility(View.INVISIBLE);
        } else {
            playerOneLeft.setVisibility(View.INVISIBLE);
            playerOneRight.setVisibility(View.INVISIBLE);
            playerTwoLeft.setVisibility(View.VISIBLE);
            playerTwoRight.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles the promotion of pawns once they reach the end of the board.
     *
     * @param position the position of the pawn when it is promoted.
     * @param team the team the pawn belongs to.
     */
    private void promotePawn(final int position, final int team) {
        // Generate an AlertDialog via the AlertDialog Builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Promote Your Pawn:")
                .setIcon(ChessPiece.getDrawableFor(ChessPiece.PAWN))
                .setView(R.layout.pawn_dialog);
        AlertDialog pawnChooser = alertDialogBuilder.create();
        pawnChooser.show();

        int color = (team == ChessPiece.BLUE_TEAM) ? ContextCompat.getColor(getContext(),
                R.color.colorPrimary) : ContextCompat.getColor(getContext(), R.color.colorAccent);

        // Change the Dialog's Icon color.
        int alertIconId = getActivity().getResources().getIdentifier("android:id/icon", null, null);
        ImageView alertIcon = (ImageView) pawnChooser.findViewById(alertIconId);
        if(alertIcon != null) {
            alertIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        // Setup the Queen Listeners and change color appropriate to the team.
        ImageView queenIcon = (ImageView) pawnChooser.findViewById(R.id.queen_icon);
        TextView queenText = (TextView) pawnChooser.findViewById(R.id.queen_text);
        if(queenIcon != null && queenText != null) {
            queenIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            queenIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            queenText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // Do the same for bishop.
        ImageView bishopIcon = (ImageView) pawnChooser.findViewById(R.id.bishop_icon);
        TextView bishopText = (TextView) pawnChooser.findViewById(R.id.bishop_text);
        if(bishopIcon != null && bishopText != null) {
            bishopIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            bishopIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            bishopText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // And the same for knight.
        ImageView knightIcon = (ImageView) pawnChooser.findViewById(R.id.knight_icon);
        TextView knightText = (TextView) pawnChooser.findViewById(R.id.knight_text);
        if(knightIcon != null && knightText != null) {
            knightIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            knightIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            knightText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // And finally, the same for rook.
        ImageView rookIcon = (ImageView) pawnChooser.findViewById(R.id.rook_icon);
        TextView rookText = (TextView) pawnChooser.findViewById(R.id.rook_text);
        if(rookIcon != null && rookText != null) {
            rookIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            rookIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            rookText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
    }

    private class Promoter implements View.OnClickListener {
        AlertDialog mDialog;
        int position;
        int team;

        Promoter(final int indexClicked, final int teamNumber, AlertDialog dialog) {
            position = indexClicked;
            team = teamNumber;
            mDialog = dialog;
        }

        @Override public void onClick(final View v) {
            int id = v.getId();
            int pieceType;
            switch (id) {
                default:
                case R.id.queen_icon:
                case R.id.queen_text:
                    pieceType = ChessPiece.QUEEN;
                    break;
                case R.id.bishop_icon:
                case R.id.bishop_text:
                    pieceType = ChessPiece.BISHOP;
                    break;
                case R.id.knight_icon:
                case R.id.knight_text:
                    pieceType = ChessPiece.KNIGHT;
                    break;
                case R.id.rook_icon:
                case R.id.rook_text:
                    pieceType = ChessPiece.ROOK;
                    break;
            }

            mBoardMap.put(position, new ChessPiece(pieceType, team));
            ImageButton promotedPieceTile = (ImageButton) mBoard.getChildAt(position);
            promotedPieceTile.setImageResource(ChessPiece.getDrawableFor(pieceType));
            boolean teamColor = team == ChessPiece.BLUE_TEAM;
            int color = teamColor ? R.color.colorPrimary: R.color.colorAccent;
            promotedPieceTile.setColorFilter(ContextCompat.getColor(getContext(), color),
                    PorterDuff.Mode.SRC_ATOP);

            mDialog.dismiss();
        }
    }

    /**
     * A View.OnClickListener that is called whenever a board tile is clicked.
     */
    private class ChessClick implements View.OnClickListener {
        @Override public void onClick(final View v) {
            int index = (int) v.getTag();
            if(mHighlightedTile != null) {
                showPossibleMoves(index);
                mHighlightedTile = null;
            } else {
                if(mBoardMap.get(index, null) != null) {
                    mHighlightedTile = (ImageButton) v;
                    showPossibleMoves(index);
                }
            }
        }
    }

}