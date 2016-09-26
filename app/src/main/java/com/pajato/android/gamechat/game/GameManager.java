/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
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

package com.pajato.android.gamechat.game;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.pajato.android.gamechat.R;

import java.util.ArrayList;

/**
 * Manages the game related aspects of the GameChat application. These include the creation of new
 * game instances, notifications, and game settings.
 *
 * @author Bryan Scott
 */
enum GameManager {
    instance;

    // Public class constants.

    // Fragment array index constants.
    public static final int NO_GAMES_INDEX = 0;
    public static final int SETTINGS_INDEX = 1;
    public static final int TTT_LOCAL_INDEX = 2;
    public static final int TTT_ONLINE_INDEX = 3;
    public static final int CHECKERS_INDEX = 4;
    public static final int CHESS_INDEX = 5;
    public static final int TOTAL_FRAGMENTS = 6;

    /** A key value for some funky stuff going on with parsing strings, aka messages. */
    public static final String GAME_KEY = "gameInit";

    /** A key value accessing the game enum ordinal value. */
    public static final String ORDINAL_KEY = "ordinallKey";

    // Public instance variables.

    /** The current match history. */
    public ArrayList<String> instructions = new ArrayList<>();

    // Private instance variables.

    /** The list of all fragments. */
    private BaseGameFragment[] mFragmentList = new BaseGameFragment[TOTAL_FRAGMENTS];

    /** The current fragment. */
    private int mCurrentFragment;

    // Public instance methods.

    /** Initialize the game manager fragment. */
    public void init(final FragmentActivity context) {
        // Initialize this fragment by using a negative index value; clear the current set of
        // instructions, establish the fragment tracking array and set the default display fragment
        // to show that there are no games to list.
        mCurrentFragment = -1;
        instructions.clear();
        mFragmentList = new BaseGameFragment[TOTAL_FRAGMENTS];
        mFragmentList[NO_GAMES_INDEX] = new ShowNoGamesFragment();
        sendNewGame(NO_GAMES_INDEX, context);
    }

    /** Return the current fragment's index value. */
    public int getCurrent() {
        return mCurrentFragment;
    }

    /** Return the current fragment being shown in the experience panel. */
    public BaseGameFragment getFragment(final int index) {
        return mFragmentList[index];
    }

    /** Return player 1 or player 2 based on the current turn value. */
    public String getTurn() {
        final int index = getCurrent();
        final Fragment context = getFragment(index);
        switch (index) {
            default:
                // These two cases should never be called in an impactful way.
            case NO_GAMES_INDEX:
            case SETTINGS_INDEX:
                return null;
            case TTT_LOCAL_INDEX:
            case TTT_ONLINE_INDEX:
                // For Tic-Tac-Toe, we need X or O.
                return getTurn(index, context, R.string.xValue, R.string.oValue);
            case CHECKERS_INDEX:
            case CHESS_INDEX:
                // For chess and checkers, we need either primary or secondary player strings.
                return getTurn(index, context, R.string.player_primary, R.string.player_secondary);
        }
    }

    /** Create and show a Snackbar notification based on the given parameters. */
    public void notify(final View view, final String output, final int color, final boolean done) {
        Snackbar notification;
        if (done) {
            // The game is ended so generate a notification that could start a new game.
            notification = Snackbar.make(view, output, Snackbar.LENGTH_INDEFINITE);
            final String playAgain = getFragment(getCurrent()).getString(R.string.PlayAgain);
            notification.setAction(playAgain, new NotificationActionHandler());
        } else {
            // The game hasn't ended so generate a notification without an action.
            notification = Snackbar.make(view, output, Snackbar.LENGTH_SHORT);
        }

        // Determine if a color has been specified. If so, set it, otherwise display the
        // notification to the User.
        if (color != -1) notification.getView().setBackgroundColor(color);
        notification.show();
    }

    /**
     * A placeholder method for a message handler / event coordinator to be implemented at a later
     * time. Currently, sendMessage sends a string to the current individual game fragment (for
     * example, TTTFragment) that it then interprets into a move.
     *
     * @param msg the message to transmit to the message handler.
     * @param fragmentIndex The index of the fragment that will handle the message.
     */
    public void sendMessage(final String msg, final int fragmentIndex) {
        instructions.add(msg);
        //TODO: replace this with an implemented event handling system.
        switch (fragmentIndex) {
            default:
                break;
            case TTT_LOCAL_INDEX:
            case TTT_ONLINE_INDEX:
            case CHECKERS_INDEX:
            case CHESS_INDEX:
                getFragment(fragmentIndex).messageHandler(msg);
                break;
        }
    }

    /** Return TRUE iff the given fragment is running in the experience panel. */
    public boolean sendNewGame(final Game game, final FragmentActivity context, final String msg) {
        int index = game.localFragmentIndex;
        return sendNewGame(index, context, msg, game);
    }

    /** Return TRUE iff the given fragment is running in the experience panel. */
    public boolean sendNewGame(final int index, final FragmentActivity context, final String msg,
                               final Game game) {
        instructions.clear();
        return setFragment(index, context, msg, game);
    }

    /** Return TRUE iff the given fragment is running in the experience panel. */
    public boolean sendNewGame(final int index, final FragmentActivity context) {
        return sendNewGame(index, context, null, null);
    }

    /** Return true iff the fragment at the given index is created and all is well. */
    public boolean setFragment(final int index, final FragmentActivity context, final String msg,
                               Game game) {
        // Ensure we're not taking any requests we can't fill.
        if (index < TOTAL_FRAGMENTS && index > -1 && index != getCurrent()) {
            // If our fragment doesn't exist yet, construct it.
            if (mFragmentList[index] == null) {
                switch (index) {
                    case TTT_LOCAL_INDEX:
                        mFragmentList[TTT_LOCAL_INDEX] = new LocalTTTFragment();
                        break;
                    case TTT_ONLINE_INDEX:
                        mFragmentList[TTT_ONLINE_INDEX] = new TTTFragment();
                        break;
                    case CHECKERS_INDEX:
                        mFragmentList[CHECKERS_INDEX] = new CheckersFragment();
                        break;
                    case CHESS_INDEX:
                        mFragmentList[CHESS_INDEX] = new ChessFragment();
                        break;
                }
                mFragmentList[index].setArguments(context.getIntent().getExtras());
            }

            // Set up the fragment in the fragment container.
            mCurrentFragment = index;
            if (msg != null || game != null) {
                Bundle bundle = new Bundle();
                bundle.putString(GAME_KEY, msg);
                bundle.putInt(ORDINAL_KEY, game.ordinal());
                mFragmentList[index].setArguments(bundle);
            }

            // Initiate the transition between fragments.
            context.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.game_pane_fragment_container, mFragmentList[index])
                    .commit();
            return true;

        } else if (msg != null) {
            sendMessage(msg, index);
            return true;
        }

        return false;
    }

    // Private instance methods.

    /** Return the string value associated with the two players based on the current turn. */
    private String getTurn(int index, Fragment context, int first, int second) {
        return getFragment(index).getTurn() ? context.getString(first) : context.getString(second);
    }

    // Inner classes.

    /** Provide an inner class to handle a notification action click. */
    private class NotificationActionHandler implements View.OnClickListener {
        @Override public void onClick(final View v) {
            // Ensure that this is truly a game fragment.  Abort if not.
            final int index = getCurrent();
            final BaseGameFragment context = getFragment(index);
            if (context == null || context.mGame == null) return;

            // There is a game being played.  Ensure that there is a valid message and send it if
            // so.
            final String primary = context.getString(context.mGame.primaryIndex);
            final String secondary = context.getString(context.mGame.secondaryIndex);
            final String message = getFragment(index).mTurn ? primary : secondary;
            final String newGameTitle = context.getString(R.string.NewGame);
            if (message != null) sendMessage(message + "\n" + newGameTitle, index);
        }
    }

}
