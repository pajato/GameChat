package com.pajato.android.gamechat.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;

import java.util.ArrayList;


public class GameFragment extends Fragment{

    /* The Match History */
    private ArrayList<String> mInstructions;
    /* Keeps track of the mTurn user. True = Player 1, False = Player 2. */
    private boolean mTurn;
    /* Player Turn Strings */
    private String mPLAYER1;
    private String mPLAYER2;

    // Game Fragments
    /* The Tic-Tac-Toe fragment */
    private TTTFragment mTicTacToe;

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Initialize the member variables.
        mPLAYER1 = getString(R.string.player_1);
        mPLAYER2 = getString(R.string.player_2);
        mInstructions = new ArrayList<>();
        mTurn = true;

        // Inflate the layout and set up the default fragment.
        View layout = inflater.inflate(R.layout.fragment_game, container, false);

        mTicTacToe = new TTTFragment();
        mTicTacToe.setArguments(getActivity().getIntent().getExtras());
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mTicTacToe).commit();

        return layout;
    }

    // Public Instance Methods

    /**
     * Sends a message alerting the event handling system that the new game button was clicked.
     *
     * @param view the new game button.
     */
    public void onNewGame(final View view) {
        // Create the message.
        String msg = getTurn() + "\n";
        msg += view.getTag().toString();

        // Empty the instructions list, as a new game has begun.
        mInstructions.clear();
        mInstructions.add(msg);

        // Output New Game Message
        String newTurn = (getTurn().equals(getString(R.string.player_1)) ?
                "Player 1 (" + getString(R.string.xValue) + ")" :
                "Player 2 (" + getString(R.string.oValue) + ")") + "'s Turn";
        Snackbar start = Snackbar.make(getActivity().findViewById(R.id.activity_main),
                "New Game! " + newTurn, Snackbar.LENGTH_SHORT);
        start.show();

        sendMessage(msg);
    }

    /**
     * Sends a message alerting the event handling system that there was a tile clicked, and
     * swaps the mTurn to the opposite player.
     *
     * @param view the tile clicked
     */
    public void tileOnClick(final View view) {
        String msg = getTurn() + "\n";
        msg = msg + view.getTag().toString();

        // Keep track of mInstructions for recreating the board.
        mInstructions.add(msg);

        sendMessage(msg);

        mTurn = !mTurn;
    }

    //Private Instance Methods

    /**
     * Gets the current mTurn and returns a string reflecting the player's
     * name who is currently playing.
     *
     * @return player 1 or player 2, depending on the mTurn.
     */
    private String getTurn() {
        return mTurn ? mPLAYER1 : mPLAYER2;
    }


    /**
     * A placeholder method for a message handler / event coordinator to be implemented at a later
     * time. Currently, sendMessage sends a string to the current individual game fragment (for
     * example, the TTTFragment) that it then interprets into a move.
     *
     * @param msg the message to transmit to the message handler.
     */
    private void sendMessage(final String msg) {
        //TODO: replace this with an implemented event handling system.
        // This will be a switch for each of the individual game fragment handlers.
        mTicTacToe.messageHandler(msg);
    }


}
