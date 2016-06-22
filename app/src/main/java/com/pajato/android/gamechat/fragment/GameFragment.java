/*
 * Copyright (C) 2016 Pajato Technologies LLC.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see http://www.gnu.org/licenses
 */

package com.pajato.android.gamechat.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.main.PaneManager;

import java.util.ArrayList;

/**
 * A Fragment that contains and controls the current game being played.
 *
 * @author Bryan Scott
 */
public class GameFragment extends BaseFragment{
    /* individual fragment indicator numbers */
    private static final int ADV_KEY = 0;
    private static final int TTT_KEY = 1;
    private static final int CHECKERS_KEY = 2;
    private static final int CHESS_KEY = 3;

    /* The Match History */
    private ArrayList<String> mInstructions;
    /* Keeps track of the Turn user. True = Player 1, False = Player 2. */
    private boolean mTurn;
    /* Player Turn Strings */
    private String mPLAYER1;
    private String mPLAYER2;
    /* Indicates the current fragment */
    private int mCurrentFragmentId;

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

        setHasOptionsMenu(true);
        // Inflate the layout and set up the default fragment.
        View layout = inflater.inflate(R.layout.fragment_game, container, false);

        mTicTacToe = new TTTFragment();
        mTicTacToe.setArguments(getActivity().getIntent().getExtras());
        getActivity().getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.fragment_container, mTicTacToe)
            .commit();

        mCurrentFragmentId = TTT_KEY;

        return layout;
    }

    // Public Instance Methods
    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        menuInflater.inflate(R.menu.game_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Determine if the new game being initiated is different than what is currently loaded.
        switch(item.getItemId()) {
            case R.id.toolbar_chat_icon:
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                break;
            default:
            case R.id.adv_new_game: onNewGame(ADV_KEY);
                break;
            case R.id.ttt_new_game: onNewGame(TTT_KEY);
                break;
            case R.id.checkers_new_game: onNewGame(CHECKERS_KEY);
                break;
            case R.id.chess_new_game: onNewGame(CHESS_KEY);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends a message alerting the event handling system that the new game button was clicked.
     *
     * @param fragmentIndicator the ID of the new game button in the floating action button menu.
     */
    public void onNewGame(final int fragmentIndicator) {
        //If we are changing games, we need to swap out for that game's fragment.
        if(fragmentIndicator != mCurrentFragmentId) {
            FragmentTransaction swap = getActivity().getSupportFragmentManager().beginTransaction();
            switch (fragmentIndicator) {
                default:
                case ADV_KEY:
                    break;
                case TTT_KEY:
                    mTicTacToe = new TTTFragment();
                    mTicTacToe.setArguments(getActivity().getIntent().getExtras());
                    swap.replace(R.id.fragment_container, mTicTacToe);
                    swap.commit();
                    mCurrentFragmentId = fragmentIndicator;
                    break;
                case CHECKERS_KEY:
                    break;
                case CHESS_KEY:
                    break;
            }
        }
        // Create the message.
        String msg = getTurn() + "\n";
        msg += getString(R.string.new_game);

        // Empty the instructions list, as a new game has begun.
        mInstructions.clear();
        mInstructions.add(msg);

        // Output New Game Messages
        String newTurn = (getTurn().equals(getString(R.string.player_1)) ?
                "Player 1 (" + getString(R.string.xValue) + ")" :
                "Player 2 (" + getString(R.string.oValue) + ")") + "'s Turn";
        Snackbar start = Snackbar.make(getActivity().findViewById(R.id.game_pane),
                "New Game! " + newTurn, Snackbar.LENGTH_SHORT);
        start.getView().setBackgroundColor(ContextCompat.getColor(getActivity(),
                R.color.colorPrimaryDark));
        start.show();

        sendMessage(msg, fragmentIndicator);
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

        sendMessage(msg, mCurrentFragmentId);

        //TODO: This causes bugs when clicking on a tile that already has a piece in it. Change.
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
    private void sendMessage(final String msg, final int fragmentIndicator) {
        //TODO: replace this with an implemented event handling system.
        // This will be a switch for each of the individual game fragment handlers.
        switch(fragmentIndicator) {
            default:
            case ADV_KEY:
                break;
            case TTT_KEY:
                mTicTacToe.messageHandler(msg);
                break;
            case CHECKERS_KEY:
                break;
            case CHESS_KEY:
                break;
        }
    }

}
