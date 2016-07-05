package com.pajato.android.gamechat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.game.GameManager;

import java.util.Scanner;

public class TTTFragment extends Fragment {

    private static final String TAG = TTTFragment.class.getSimpleName();
    /* Keeps track of the Turn user. True = Player 1, False = Player 2. */
    public boolean mTurn;

    // Keeps track of the turn number
    private int mTurnCount;

    // Player Piece Strings
    private String mXValue;
    private String mOValue;

    // The mBoard view
    private View mBoard;

    public TTTFragment() {

    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        mXValue = getString(R.string.xValue);
        mOValue = getString(R.string.oValue);
        mTurn = true;

        // Initialize the turn counter, and mBoard variables.
        mTurnCount = 9;
        mBoard = inflater.inflate(R.layout.fragment_ttt, container, false);
        return mBoard;
    }

    /**
     * Translates the messages sent by the event system and handles the individual clicks
     * based on messages sent.
     *
     * @param msg the message to be handled.
     */
    public void messageHandler(String msg) {
        //TODO: Modify this when an implemented event handling system is implemented.
        Scanner input = new Scanner(msg);
        String player = input.nextLine();
        String buttonTag = input.nextLine();
        input.close();
        // Call appropriate methods for each button.
        if(buttonTag.equals(getString(R.string.new_game))) {
            handleNewGame(player);
        } else {
            handleTileClick(player, buttonTag);
        }

    }

    /**
     * Empties the instructions, mBoard tiles, and outputs a new game message.
     *
     * @param player the player who initiated the click.
     */
    private void handleNewGame(String player) {
        // Reset initial variables and the Winner / Turn views.
        mTurnCount = 9;
        TextView Winner = (TextView) super.getActivity().findViewById(R.id.winner);
        Winner.setVisibility(View.INVISIBLE);

        // Set values for each tile to empty.
        ((Button) mBoard.findViewWithTag("button00")).setText(getString(R.string.spaceValue));
        ((Button) mBoard.findViewWithTag("button01")).setText(getString(R.string.spaceValue));
        ((Button) mBoard.findViewWithTag("button02")).setText(getString(R.string.spaceValue));
        ((Button) mBoard.findViewWithTag("button10")).setText(getString(R.string.spaceValue));
        ((Button) mBoard.findViewWithTag("button11")).setText(getString(R.string.spaceValue));
        ((Button) mBoard.findViewWithTag("button12")).setText(getString(R.string.spaceValue));
        ((Button) mBoard.findViewWithTag("button20")).setText(getString(R.string.spaceValue));
        ((Button) mBoard.findViewWithTag("button21")).setText(getString(R.string.spaceValue));
        ((Button) mBoard.findViewWithTag("button22")).setText(getString(R.string.spaceValue));

        // Ensure the icons are correctly indicating whose turn it is.
        handlePlayerIcons(mTurn);

        // Output New Game Messages
        String newTurn = "New Game! Player " + (mTurn ? "1 (" + getString(R.string.xValue) + ")" :
                "2 (" + getString(R.string.oValue) + ")") + "'s Turn";
        GameManager.instance.generateSnackbar(mBoard, newTurn, ContextCompat.getColor(getActivity(),
                R.color.colorPrimaryDark), false);

        checkNotFinished();
    }

    /**
     * Assigns a value to a button without text, either X or O,
     * depending on the player whose turn it is.
     *
     * @param player the player who initiated the click.
     * @param buttonTag the tag of the button clicked.
     */
    private void handleTileClick(String player, String buttonTag) {
        Button b = (Button) mBoard.findViewWithTag(buttonTag);
        // Only changes the value if the current value is empty and the game has not finished yet.
        if (b.getText().toString().equals(getString(R.string.spaceValue)) && checkNotFinished()) {
            mTurnCount--;

            b.setText(getTurn(mTurn));

            mTurn = !mTurn;

            handlePlayerIcons(mTurn);

            checkNotFinished();

        }
    }

    /**
     * Returns a string enumerating the player, depending on the current turn.
     *
     * @return R.string.xValue or R.string.oValue, depending on whose turn it is.
     */
    private String getTurn(boolean turnIndicator) {
        if(turnIndicator) {
            return mXValue;
        } else {
            return mOValue;
        }
    }

    /**
     * Evaluates the current state of the individual tiles of the mBoard and stores them.
     */
    private void evaluateBoard(int[][] boardValues) {
        // Go through all the buttons.
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                Button currTile = (Button) mBoard.findViewWithTag("button" + Integer.toString(i) + Integer.toString(j));
                String tileValue = currTile.getText().toString();

                // Assign each possible state for each tile as a value. The only possible values
                // of each row (that indicate and endgame state) are 3 in a row of X or O. There
                // is no way to get a value of 3 without getting 3 Xs, and there's no way to get a
                // value of 6 without getting 3 Os.

                // If there's an X in this button, store a 1
                if(tileValue.equals(mXValue)) {
                    boardValues[i][j] = 1;
                // If there's an O in this button, store a 2
                } else if (tileValue.equals(mOValue)) {
                    boardValues[i][j] = 2;
                // Otherwise, there's a space. -5 was chosen arbitrarily to keep row values unique.
                } else {
                    boardValues[i][j] = -5;
                }
            }
        }
    }

    /**
     * Evaluates the state of the mBoard, determining if there are three in a row of either X or O.
     *
     * @return false if a player has won or if the full number of turns has occurred, true otherwise.
     */
    private boolean checkNotFinished() {
        int[][] boardValues = new int[3][3];
        // First, we need to check on the buttons' states.
        evaluateBoard(boardValues);

        // Evaluate all possible lines of 3.
        int topRow = boardValues[0][0] + boardValues[0][1] + boardValues[0][2];
        int midRow = boardValues[1][0] + boardValues[1][1] + boardValues[1][2];
        int botRow = boardValues[2][0] + boardValues[2][1] + boardValues[2][2];

        int startCol = boardValues[0][0] + boardValues[1][0] + boardValues[2][0];
        int centerCol = boardValues[0][1] + boardValues[1][1] + boardValues[2][1];
        int endCol = boardValues[0][2] + boardValues[1][2] + boardValues[2][2];

        int leftDiag = boardValues[0][0] + boardValues[1][1] + boardValues[2][2];
        int rightDiag = boardValues[2][0] + boardValues[1][1] + boardValues [0][2];

        // If any lines of 3 are equal to 3, X wins.
        boolean xWins = (topRow == 3 || midRow == 3 || botRow == 3 || startCol == 3
                || centerCol == 3 || endCol == 3 || leftDiag == 3 || rightDiag == 3);

        // If any lines of 3 are equal to 6, O wins.
        boolean oWins = (topRow == 6 || midRow == 6 || botRow == 6 || startCol == 6
                || centerCol == 6 || endCol == 6 || leftDiag == 6 || rightDiag == 6);

        // If we have a win condition, reveal the winning messages.
        if(xWins || oWins || mTurnCount == 0) {
            // Setup the winner TextView and snackbar messages.
            TextView Winner = (TextView) super.getActivity().findViewById(R.id.winner);
            Winner.setText(getText(R.string.spaceValue));
            Winner.setVisibility(View.VISIBLE);

            if(xWins) {
                Winner.setText(R.string.winner_x);
                handlePlayerIcons(true);
                GameManager.instance.generateSnackbar(mBoard, "Player 1 (" + mXValue + ") Wins!",
                        ContextCompat.getColor(getContext(), R.color.colorPrimaryDark), true);
            } else if (oWins) {
                Winner.setText(R.string.winner_o);
                handlePlayerIcons(false);
                GameManager.instance.generateSnackbar(mBoard, "Player 2 (" + mOValue + ") Wins!",
                        ContextCompat.getColor(getContext(), R.color.colorPrimaryDark), true);

            // If no one has won, the turn timer has run out. End the game.
            } else {
                // Reveal Tie Messages
                Winner.setText(R.string.winner_tie);
                GameManager.instance.generateSnackbar(mBoard, "It's a Tie!", -1, true);
            }
            return false;
        }
        // If none of the conditions are met, the game has not yet ended, and we can continue it.
        return true;
    }

    /**
     * A method that handles the management of a the turn indicators. If it is a player's turn,
     * their icon is increased in size, changed to the accented color, and emphasized with auxiliary
     * text views. The other player's icon decreases in size, is turned a dark color, and is no
     * longer emphasized.
     *
     * @param turn differentiates between the turn. true = Player 1's turn, false = Player 2's turn.
     */
    private void handlePlayerIcons(boolean turn) {
        final float LARGE = 60.0f;
        final float SMALL = 45.0f;

        // Collect all the pertinent textViews.
        TextView p1 = (TextView) getActivity().findViewById(R.id.player_1_icon);
        TextView p2 = (TextView) getActivity().findViewById(R.id.player_2_icon);

        TextView p1left = (TextView) getActivity().findViewById(R.id.player_1_left_indicator);
        TextView p1right = (TextView) getActivity().findViewById(R.id.player_1_right_indicator);

        TextView p2left = (TextView) getActivity().findViewById(R.id.player_2_left_indicator);
        TextView p2right = (TextView) getActivity().findViewById(R.id.player_2_right_indicator);

        if(turn) {
            // If it's player 1's turn, make their icon bigger and accented...
            p1.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE);
            p1.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            p1left.setVisibility(View.VISIBLE);
            p1right.setVisibility(View.VISIBLE);
            // and de-emphasize player 2's icon.
            p2.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL);
            p2.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            p2left.setVisibility(View.INVISIBLE);
            p2right.setVisibility(View.INVISIBLE);
        } else {
            // If it's player 2's turn, make their icon bigger and accented...
            p1.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL);
            p1.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            p1left.setVisibility(View.INVISIBLE);
            p1right.setVisibility(View.INVISIBLE);
            // and de-emphasize player 1's icon.
            p2.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE);
            p2.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            p2left.setVisibility(View.VISIBLE);
            p2right.setVisibility(View.VISIBLE);
        }
    }

}
