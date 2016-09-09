package com.pajato.android.gamechat.game;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseFragment;

import java.util.HashMap;
import java.util.Scanner;

/**
 * A Tic-Tac-Toe game that stores its current state on Firebase, allowing for cross-device play.
 *
 * @author Bryan Scott
 */
public class TTTFragment extends BaseFragment {

    private static final String TAG = TTTFragment.class.getSimpleName();
    private static final String FIREBASE_URL = "https://gamechat-1271.firebaseio.com/boards/ticTacToe";
    private static final String TURN_INDICATOR = "turnIndicator";

    /* Keeps track of the Turn user. True = Player 1, False = Player 2. */
    public boolean mTurn;

    // Player Piece Strings
    private String mXValue;
    private String mOValue;
    private String mSpace;

    // Board management objects
    private HashMap<String, Integer> mLayoutMap;
    private Firebase mRef;

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_ttt;}

    @Override public void onInitialize() {
        // Initialize Member Variables
        super.onInitialize();
        mXValue = getString(R.string.xValue);
        mOValue = getString(R.string.oValue);
        mSpace = getString(R.string.spaceValue);
        mTurn = true;

        getActivity().findViewById(R.id.games_fab).setVisibility(View.VISIBLE);

        // Setup our Firebase database reference and a listener to keep track of the board.
        Firebase.setAndroidContext(getContext());
        mRef = new Firebase(FIREBASE_URL);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            // If the data changes on Firebase, we want to capture the new board and replicate it locally.
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, Integer>> t =
                        new GenericTypeIndicator<HashMap<String, Integer>>() {};
                mLayoutMap = dataSnapshot.getValue(t);
                // If there is a board to be had, fill our board out with it and adjust the turn.
                if(mLayoutMap != null) {
                    recreateExistingBoard();
                    checkNotFinished();
                    mTurn = (mLayoutMap.get(TURN_INDICATOR) == 1);
                    handlePlayerIcons(mTurn);
                } else {
                    mLayoutMap = new HashMap<>();
                }
            }
            // In the event that the request to listen is canceled, we need to
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "Error reading Firebase board data: " + firebaseError.toString());
            }
        });

    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ttt_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.options_menu_new_ttt) {
            GameManager.instance.sendNewGame(GameManager.TTT_LOCAL_INDEX, getActivity(),
                    GameManager.instance.getTurn() + "\n" + "New Game");
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Translates the messages sent by the event system and handles the individual clicks
     * based on messages sent.
     *
     * @param msg the message to be handled.
     */
    public void messageHandler(final String msg) {
        //TODO: Modify this when an implemented event handling system is implemented.
        Scanner input = new Scanner(msg);
        String buttonTag = input.nextLine();
        input.close();

        // Call appropriate methods for each button.
        if(buttonTag.equals(getString(R.string.new_game))) {
            handleNewGame();
        } else {
            handleTileClick(buttonTag);
        }

    }

    /**
     * Evaluates the state of the board, determining if there are three in a row of either X or O.
     *
     * @return false if a player has won or if the full number of turns has occurred, true otherwise.
     */
    private boolean checkNotFinished() {
        // First, we need to check on the buttons' states.
        int[][] boardValues = evaluateBoard();

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
        if(xWins || oWins || mLayoutMap.size() == 10) {
            // Setup the winner TextView and snackbar messages.
            TextView Winner = (TextView) super.getActivity().findViewById(R.id.winner);
            Winner.setText(getText(R.string.spaceValue));
            Winner.setVisibility(View.VISIBLE);

            // If there is a winner, output winning messages and ensure that the appropriate
            // player's icon has been highlighted.
            if(xWins) {
                Winner.setText(R.string.winner_x);
                handlePlayerIcons(true);
                GameManager.instance.generateSnackbar(mLayout, "Player 1 (" + mXValue + ") Wins!",
                        ContextCompat.getColor(getContext(), R.color.colorPrimaryDark), true);
            } else if (oWins) {
                Winner.setText(R.string.winner_o);
                handlePlayerIcons(false);
                GameManager.instance.generateSnackbar(mLayout, "Player 2 (" + mOValue + ") Wins!",
                        ContextCompat.getColor(getContext(), R.color.colorPrimaryDark), true);

            // If no one has won, the turn timer has run out. End the game.
            } else {
                // Reveal Tie Messages
                Winner.setText(R.string.winner_tie);
                GameManager.instance.generateSnackbar(mLayout, "It's a Tie!", -1, true);
            }
            return false;
        }
        // If none of the conditions are met, the game has not yet ended, and we can continue it.
        return true;
    }

    /**
     * Evaluates the current state of the individual tiles of the board and stores them as a HashMap
     * in the Firebase Database.
     */
    private int[][] evaluateBoard() {
        int[][] boardValues = new int[3][3];
        if(mLayoutMap == null) mLayoutMap = new HashMap<>();
        // Go through all the buttons.
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                Button currTile = (Button) mLayout.findViewWithTag("button" + Integer.toString(i) + Integer.toString(j));
                String tileValue = currTile.getText().toString();

                // Assign each possible state for each tile as a value. The only possible values
                // of each row (that indicate and endgame state) are 3 in a row of X or O. There
                // is no way to get a value of 3 without getting 3 Xs, and there's no way to get a
                // value of 6 without getting 3 Os.

                // If there's an X in this button, store a 1
                if(tileValue.equals(mXValue)) {
                    boardValues[i][j] = 1;
                    if(!(mLayoutMap.containsKey(i + "-" + j))) mLayoutMap.put(i + "-" + j, 1);
                    // If there's an O in this button, store a 2
                } else if (tileValue.equals(mOValue)) {
                    boardValues[i][j] = 2;
                    if(!(mLayoutMap.containsKey(i + "-" + j))) mLayoutMap.put(i + "-" + j, 2);
                    // Otherwise, there's a space. -5 was chosen arbitrarily to keep row values unique.
                } else {
                    boardValues[i][j] = -5;
                }
            }
        }
        mRef.setValue(mLayoutMap);
        return boardValues;
    }

    /**
     * Returns a string enumerating the player, depending on the current turn.
     *
     * @return R.string.xValue or R.string.oValue, depending on whose turn it is.
     */
    private String getTurn(final boolean turnIndicator) {
        if(turnIndicator) {
            return mXValue;
        } else {
            return mOValue;
        }
    }

    /**
     * A method that handles the management of a the turn indicators. If it is a player's turn,
     * their icon is increased in size, changed to the accented color, and emphasized with auxiliary
     * text views. The other player's icon decreases in size, is turned a dark color, and is no
     * longer emphasized.
     *
     * @param turn differentiates between the turn. true = Player 1's turn, false = Player 2's turn.
     */
    private void handlePlayerIcons(final boolean turn) {
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

    /**
     * Empties the instructions, board tiles, and outputs a new game message.
     */
    private void handleNewGame() {
        // Reset the board map and update the Firebase database's copy.
        if(mLayoutMap != null) {
            mLayoutMap.clear();
        } else {
            mLayoutMap = new HashMap<>();
        }
        mLayoutMap.put(TURN_INDICATOR, (mTurn ? 1 : 2));
        mRef.setValue(mLayoutMap);

        // Hide our winning messages and ensure the turn display is working properly.
        TextView Winner = (TextView) super.getActivity().findViewById(R.id.winner);
        Winner.setVisibility(View.INVISIBLE);
        handlePlayerIcons(mTurn);

        // Set values for each tile to empty.
        ((Button) mLayout.findViewWithTag("button00")).setText(mSpace);
        ((Button) mLayout.findViewWithTag("button01")).setText(mSpace);
        ((Button) mLayout.findViewWithTag("button02")).setText(mSpace);
        ((Button) mLayout.findViewWithTag("button10")).setText(mSpace);
        ((Button) mLayout.findViewWithTag("button11")).setText(mSpace);
        ((Button) mLayout.findViewWithTag("button12")).setText(mSpace);
        ((Button) mLayout.findViewWithTag("button20")).setText(mSpace);
        ((Button) mLayout.findViewWithTag("button21")).setText(mSpace);
        ((Button) mLayout.findViewWithTag("button22")).setText(mSpace);

        // Output New Game Messages
        String newTurn = "New Game! Player " + (mTurn
                ? "1 (" + mXValue + ")"
                : "2 (" + mOValue + ")") + "'s Turn";
        GameManager.instance.generateSnackbar(mLayout, newTurn, ContextCompat.getColor(getActivity(),
                R.color.colorPrimaryDark), false);

        checkNotFinished();
    }

    /**
     * Assigns a value to a button without text, either X or O,
     * depending on the player whose turn it is.
     *
     * @param buttonTag the tag of the button clicked.
     */
    private void handleTileClick(final String buttonTag) {
        Button b = (Button) mLayout.findViewWithTag(buttonTag);
        // Only updates the tile if the current value is empty and the game has not finished yet.
        if (b.getText().toString().equals(getString(R.string.spaceValue)) && checkNotFinished()) {
            b.setText(getTurn(mTurn));
            mLayoutMap.put(TURN_INDICATOR, mTurn ? 2 : 1);
            checkNotFinished();
        }
    }

    /**
     * Handles the recreation of the board based on the values stored in the BoardMap.
     */
    private void recreateExistingBoard() {
        // If the board map is size 1, we know that there is only the turn stored in it, and send a
        // new game out.
        if(mLayoutMap.size() == 1) {
            GameManager.instance.sendNewGame(GameManager.TTT_ONLINE_INDEX, getActivity(),
                    getTurn(mTurn) + "\n" + getString(R.string.new_game));
        // Otherwise, we'll need to comb through the board and replace the remaining pieces.
        } else {
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    // If our keys are present, then put their corresponding piece onto the board.
                    String currKey = i + "-" + j;
                    Button currButton = (Button) mLayout.findViewWithTag("button" + i + j);
                    if(mLayoutMap.containsKey(currKey)) {
                        if(mLayoutMap.get(currKey) == 1) {
                            currButton.setText(mXValue);
                        } else {
                            currButton.setText(mOValue);
                        }
                    // Otherwise, empty the tile.
                    } else {
                        currButton.setText(mSpace);
                    }
                }
            }
        }
    }

}
