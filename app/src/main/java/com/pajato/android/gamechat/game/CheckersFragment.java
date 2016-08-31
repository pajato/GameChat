package com.pajato.android.gamechat.game;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
 * A simple Checkers game for use in GameChat.
 *
 * @author Bryan Scott
 */
public class CheckersFragment extends BaseFragment {
    private static final int PRIMARY_PIECE = 1;
    private static final int PRIMARY_KING = 2;
    private static final int SECONDARY_PIECE = 3;
    private static final int SECONDARY_KING = 4;

    // Designates the turn. true = primary; false = secondary.
    public boolean mTurn;

    // Board Management Objects
    private GridLayout mBoard;
    private SparseIntArray mBoardMap;
    private ImageButton mHighlightedTile;
    private boolean mIsHighlighted = false;
    private View mLayout;
    private ArrayList<Integer> mPossibleMoves;

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_checkers, container, false);
        mBoard = (GridLayout) mLayout.findViewById(R.id.board);
        mTurn = true;
        onNewGame();

        getActivity().findViewById(R.id.games_fab).setVisibility(View.VISIBLE);
        setHasOptionsMenu(true);

        // Color the turn tiles.
        ImageView playerOneIcon = (ImageView) mLayout.findViewById(R.id.player_1_icon);
        playerOneIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary),
                PorterDuff.Mode.SRC_ATOP);

        ImageView playerTwoIcon = (ImageView) mLayout.findViewById(R.id.player_2_icon);
        playerTwoIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent),
                PorterDuff.Mode.SRC_ATOP);

        return mLayout;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.checkers_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.options_menu_new_checkers) {
            GameManager.instance.sendNewGame(GameManager.CHECKERS_INDEX, getActivity(),
                    GameManager.instance.getTurn() + "\n" + "New Game");
        }
        return super.onOptionsItemSelected(item);
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
            if(player.equals(getString(R.string.player_secondary))) {
                color = ContextCompat.getColor(getContext(), R.color.colorAccent);
            } else {
                player = getString(R.string.player_primary);
                color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            }
            GameManager.instance.generateSnackbar(mLayout, getString(R.string.new_game) + "! "
                    + player + "'s Turn!", color, false);
        }

    }

    /**
     * Handles a new game of checkers, resetting the board.
     */
    private void onNewGame() {
        mBoard.removeAllViews();
        mBoardMap = new SparseIntArray();
        mBoardMap.clear();
        mPossibleMoves = new ArrayList<>();
        mPossibleMoves.clear();

        // Go through and populate the GridLayout / Board.
        for(int i = 0; i < 64; i++) {
            ImageButton currentTile = new ImageButton(getContext());

            // Set up the gridlayout params, so that each cell is functionally identical.
            int screenWidth = getActivity().findViewById(R.id.game_pane_fragment_container)
                    .getWidth();
            int pieceSideLength = screenWidth / 8;
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = pieceSideLength;
            param.width = pieceSideLength;
            param.rightMargin = 0;
            param.topMargin = 0;
            param.setGravity(Gravity.CENTER);
            param.rowSpec = GridLayout.spec(i / 8);
            param.columnSpec = GridLayout.spec(i % 8);

            // Set up the Tile-specific information.
            currentTile.setLayoutParams(param);
            currentTile.setTag(i);

            // Handle the checkerboard positions.
            boolean isEven = (i % 2 == 0);
            boolean isOdd = (i % 2 == 1);
            boolean evenRowEvenColumn = ((i / 8) % 2 == 0) && isEven;
            boolean oddRowOddColumn = ((i / 8) % 2 == 1) && isOdd;

            // Handle the starting piece positions.
            boolean isTopRow = (-1 < i && i < 8) && isEven;
            boolean isSecondRow = (7 < i && i < 16) && isOdd;
            boolean isThirdRow = (15 < i && i < 24) && isEven;

            boolean isThirdBottomRow = (39 < i && i < 48) && isOdd;
            boolean isSecondBottomRow = (47 < i && i < 56) && isEven;
            boolean isBottomRow = (55 < i && i < 64) && isOdd;

            boolean containsSecondaryPiece = isTopRow || isSecondRow || isThirdRow;
            boolean containsPrimaryPiece = isThirdBottomRow || isSecondBottomRow || isBottomRow;

            // Create the checkerboard pattern on the button backgrounds.
            if(evenRowEvenColumn || oddRowOddColumn) {
                currentTile.setBackgroundColor(ContextCompat.getColor(
                        getContext(), android.R.color.white));
                currentTile.setImageResource(0);
            } else {
                currentTile.setBackgroundColor(ContextCompat.getColor(
                        getContext(), android.R.color.darker_gray));
                currentTile.setImageResource(0);
            }

            // If the tile is meant to contain a board piece at the start of play, give it a piece.
            if(containsSecondaryPiece) {
                currentTile.setImageResource(R.drawable.ic_account_circle_black_36dp);
                currentTile.setColorFilter(ContextCompat.getColor(getContext(),
                        R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                mBoardMap.put(i, SECONDARY_PIECE);
            } else if (containsPrimaryPiece) {
                currentTile.setImageResource(R.drawable.ic_account_circle_black_36dp);
                currentTile.setColorFilter(ContextCompat.getColor(getContext(),
                        R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                mBoardMap.put(i, PRIMARY_PIECE);
            }
            currentTile.setOnClickListener(new CheckersClick());
            mBoard.addView(currentTile);
        }

        handleTurnChange(false);
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
            mHighlightedTile.setBackgroundColor(ContextCompat.getColor(getContext(),
                    android.R.color.white));

            for (int possiblePosition : mPossibleMoves) {
                if(possiblePosition != -1 && mBoardMap.get(possiblePosition, -1) == -1) {

                    // If the tile clicked is one of the possible positions, and it's the correct
                    // turn/piece combination, the piece moves there.
                    if(indexClicked == possiblePosition) {
                        boolean capturesPiece = (indexClicked > 9 + highlightedIndex) ||
                                (indexClicked < highlightedIndex - 9);

                        if(mTurn && (mBoardMap.get(highlightedIndex) == PRIMARY_PIECE ||
                                mBoardMap.get(highlightedIndex) == PRIMARY_KING)) {

                            handleMovement(true, indexClicked, capturesPiece);

                        } else if(!mTurn && (mBoardMap.get(highlightedIndex) == SECONDARY_PIECE
                                || mBoardMap.get(highlightedIndex) == SECONDARY_KING)) {

                            handleMovement(false, indexClicked, capturesPiece);
                        }
                    }
                    // Clear the highlight off of all possible positions.
                    mBoard.getChildAt(possiblePosition).setBackgroundColor(ContextCompat
                            .getColor(getContext(), android.R.color.white));
                }
            }
            mHighlightedTile = null;

        // Otherwise, we need to highlight the tile clicked and its potential move squares with red.
        } else {
            mHighlightedTile.setBackgroundColor(ContextCompat.getColor(getContext(),
                    android.R.color.holo_red_dark));
            for(int possiblePosition : mPossibleMoves) {
                if(possiblePosition != -1 && mBoardMap.get(possiblePosition, -1) == -1) {
                    mBoard.getChildAt(possiblePosition).setBackgroundColor(ContextCompat
                            .getColor(getContext(), android.R.color.holo_red_light));
                }
            }
        }

        mIsHighlighted = !mIsHighlighted;
    }

    /**
     * Checks to see if the game is over or not by counting the number of primary / secondary pieces
     * on the board. If there are zero of one type of pieces, then the other side wins.
     *
     * @return true if the game is over, false otherwise.
     */
    private boolean checkFinished() {
        // Generate win conditions. If one side runs out of pieces, the other side wins.
        int yCount = 0;
        int bCount = 0;
        for(int i = 0; i < 64; i++) {
            int tmp = mBoardMap.get(i);
            if(tmp == PRIMARY_PIECE || tmp == PRIMARY_KING) {
                bCount++;
            } else if (tmp == SECONDARY_PIECE || tmp == SECONDARY_KING) {
                yCount++;
            }
        }
        // Verify win conditions. If one passes, return true and generate an endgame snackbar.
        if(yCount == 0) {
            GameManager.instance.generateSnackbar(mBoard, "Game Over! Player 1 Wins!",
                    ContextCompat.getColor(getContext(), R.color.colorPrimary), true);
            return true;
        } else if (bCount == 0) {
            GameManager.instance.generateSnackbar(mBoard, "Game Over! Player 2 Wins!",
                    ContextCompat.getColor(getContext(), R.color.colorPrimaryDark), true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Finds the "jumpable" pieces that the current piece could potentially capture.
     *
     * @param highlightedIndex the current index of the tile looking to jump.
     * @param jumpable the index of the tile that the piece can potentially jump to.
     */
    private void findJumpables(final int highlightedIndex, final int jumpable,
                               final ArrayList<Integer> movementOptions) {
        // Create the boolean calculations for each of our conditions.
        boolean withinBounds = jumpable < 64 && jumpable > -1;
        boolean emptySpace = mBoardMap.get(jumpable, -1) == -1;
        boolean breaksBorders = (highlightedIndex % 8 == 1 && jumpable % 8 == 7)
                || (highlightedIndex % 8 == 6 && jumpable % 8 == 0);
        boolean jumpsAlly = false;

        // Check if the piece being jumped is an ally piece.
        int highlightedPieceType = mBoardMap.get(highlightedIndex);
        int jumpedIndex = (highlightedIndex + jumpable) / 2;
        int jumpedPieceType = mBoardMap.get(jumpedIndex);

        if (highlightedPieceType == PRIMARY_PIECE || highlightedPieceType == PRIMARY_KING) {
            jumpsAlly = jumpedPieceType == PRIMARY_PIECE || jumpedPieceType == PRIMARY_KING;

        } else if (highlightedPieceType == SECONDARY_PIECE
                || highlightedPieceType == SECONDARY_KING) {
            jumpsAlly = jumpedPieceType == SECONDARY_PIECE || jumpedPieceType == SECONDARY_KING;
        }

        if(withinBounds && emptySpace && !breaksBorders && !jumpsAlly) {
            movementOptions.add(jumpable);
        }
    }

    /**
     * Locates the possible moves of the piece that is about to be highlighted.
     *
     * @param highlightedIndex the index containing the highlighted piece.
     */
    private void findPossibleMoves(final int highlightedIndex,
                                   final ArrayList<Integer> possibleMoves) {
        if(highlightedIndex < 0 || highlightedIndex > 64) {
            return;
        }

        possibleMoves.clear();
        int highlightedPieceType = mBoardMap.get(highlightedIndex);

        // Get the possible positions, post-move, for the piece.
        int upLeft = highlightedIndex - 9;
        int upRight = highlightedIndex - 7;
        int downLeft = highlightedIndex + 7;
        int downRight = highlightedIndex + 9;

        // Handle vertical edges of the board and non-king pieces.
        if(highlightedIndex / 8 == 0 || highlightedPieceType == SECONDARY_PIECE) {
            upLeft = -1;
            upRight = -1;
        } else if (highlightedIndex / 8 == 7 || highlightedPieceType == PRIMARY_PIECE) {
            downLeft = -1;
            downRight = -1;
        }

        // Handle horizontal edges of the board.
        if(highlightedIndex % 8 == 0) {
            upLeft = -1;
            downLeft = -1;
        } else if(highlightedIndex % 8 == 7) {
            upRight = -1;
            downRight = -1;
        }

        // Handle tiles that already contain other pieces. You can jump over enemy pieces,
        // but not allied pieces.
        if(mBoardMap.get(upLeft, -1) != -1) {
            findJumpables(highlightedIndex, upLeft - 9, possibleMoves);
            upLeft = -1;
        }
        if(mBoardMap.get(upRight, -1) != -1) {
            findJumpables(highlightedIndex, upRight - 7, possibleMoves);
            upRight = -1;
        }
        if(mBoardMap.get(downLeft, -1) != -1) {
            findJumpables(highlightedIndex, downLeft + 7, possibleMoves);
            downLeft = -1;
        }
        if(mBoardMap.get(downRight, -1) != -1) {
            findJumpables(highlightedIndex, downRight + 9, possibleMoves);
            downRight = -1;
        }

        // Put the values in our int array to return
        possibleMoves.add(upLeft);
        possibleMoves.add(upRight);
        possibleMoves.add(downLeft);
        possibleMoves.add(downRight);
    }

    /**
     * Handles the movement of the pieces.
     *
     * @param player indicates the current player. True is Primary, False is Secondary.
     * @param indexClicked the index of the clicked tile and the new position of the piece.
     */
    private void handleMovement(final boolean player, final int indexClicked,
                                final boolean capturesPiece) {
        // Reset the highlighted tile's image.
        mHighlightedTile.setImageResource(0);
        int highlightedIndex = (int) mHighlightedTile.getTag();
        int highlightedPieceType = mBoardMap.get(highlightedIndex);

        // Check to see if our piece becomes a king piece and put its value into the board map.
        if(indexClicked < 8 && highlightedPieceType == PRIMARY_PIECE) {
            mBoardMap.put(indexClicked, PRIMARY_KING);
        } else if (indexClicked > 55 && highlightedPieceType == SECONDARY_PIECE) {
            mBoardMap.put(indexClicked, SECONDARY_KING);
        } else {
            mBoardMap.put(indexClicked, highlightedPieceType);
        }

        // Find the new tile and give it a piece.
        ImageButton newLocation = (ImageButton) mBoard.getChildAt(indexClicked);
        if(mBoardMap.get(indexClicked) == PRIMARY_KING ||
                mBoardMap.get(indexClicked) == SECONDARY_KING) {
            newLocation.setImageResource(R.drawable.ic_stars_black_36dp);
        } else {
            newLocation.setImageResource(R.drawable.ic_account_circle_black_36dp);
        }

        // Color the piece according to the player.
        int color;
        if(player) {
            color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        } else {
            color = ContextCompat.getColor(getContext(), R.color.colorAccent);
        }
        newLocation.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        // Handle capturing pieces.
        boolean finishedJumping = true;
        if(capturesPiece) {
            int pieceCapturedIndex = (indexClicked + highlightedIndex) / 2;
            ImageButton capturedTile = (ImageButton) mBoard.getChildAt(pieceCapturedIndex);
            capturedTile.setImageResource(0);
            mBoardMap.delete(pieceCapturedIndex);

            // If there are no more jumps, change turns. If there is at least one jump left, don't.
            ArrayList<Integer> possibleJumps = new ArrayList<>();
            findPossibleMoves(indexClicked, possibleJumps);
            for(int possiblePosition: possibleJumps) {
                if(possiblePosition != -1 && (possiblePosition > 9 + indexClicked
                        || (possiblePosition < indexClicked - 9))) {
                    finishedJumping = false;
                }
            }
        }

        mBoardMap.delete(highlightedIndex);
        handleTurnChange(finishedJumping);
        checkFinished();
    }

    /**
     * Handles changing the turn and turn indicator.
     */
    private void handleTurnChange(final boolean switchPlayer) {
        if(switchPlayer) {
            mTurn = !mTurn;
        }

        // Handle the textviews that serve as our turn indicator.
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
     * A View.OnClickListener that is called whenever a board tile is clicked.
     */
    private class CheckersClick implements View.OnClickListener {
        @Override public void onClick(final View v) {
            int index = (int) v.getTag();
            if(mHighlightedTile != null) {
                showPossibleMoves(index);
                mHighlightedTile = null;
            } else {
                if(mBoardMap.get(index, -1) != -1) {
                    mHighlightedTile = (ImageButton) v;
                    showPossibleMoves(index);
                }
            }
        }
    }

}
