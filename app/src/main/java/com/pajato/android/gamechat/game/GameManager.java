/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.pajato.android.gamechat.game;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.fragment.SettingsFragment;
import com.pajato.android.gamechat.fragment.TTTFragment;

import java.util.ArrayList;

/**
 * Manages the game related aspects of the GameChat application. These include the creation of new
 * game instances, notifications, and game settings.
 *
 * @author Bryan Scott
 */
public enum GameManager {
    instance;

    // Public Class Constants
    public static final int SETTINGS_INDEX = 0;
    public static final int TTT_INDEX = 1;
    public static final int CHECKERS_INDEX = 2;
    public static final int CHESS_INDEX = 3;

    // Public Instance Variables
    /** The Current Match History */
    public ArrayList<String> mInstructions = new ArrayList<>();

    // Private class constants
    /** The logcat tag constant. */
    private static final String TAG = GameManager.class.getSimpleName();

    // Private instance variables
    /** Contains the list of all the fragments. */
    private ArrayList<Fragment> fragmentList = new ArrayList<>();
    /** Contains the fragment index */
    private int currentFragment;

    // Public instance methods

    public void init(final FragmentActivity context) {
        currentFragment = -1;
        mInstructions.clear();
        fragmentList.clear();
        //TODO: Implement these and uncomment them when they are implemented.
        fragmentList.add(new SettingsFragment());
        fragmentList.add(new TTTFragment());
        //fragmentList.add(new CheckersFragment());
        //fragmentList.add(new ChessFragment());

        // Set the current fragment to our default.
        setCurrentFragment(SETTINGS_INDEX, context, null);
    }

    /**
     * Gets a fragment by their index.
     *
     * @return The current fragment loaded up in our fragment container.
     */
    public Fragment getFragment(int index) {
        return fragmentList.get(index);
    }

    /**
     * A getter for the current fragment's index.
     *
     * @return the index of the current fragment.
     */
    public int getCurrentFragmentIndex() {
        return currentFragment;
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
        mInstructions.add(msg);
        //TODO: replace this with an implemented event handling system.
        switch(fragmentIndex) {
            default:
            case GameManager.SETTINGS_INDEX:
                break;
            case GameManager.TTT_INDEX:
                ((TTTFragment) GameManager.instance.getFragment(GameManager.TTT_INDEX)).messageHandler(msg);
                break;
            case GameManager.CHECKERS_INDEX:
                break;
            case GameManager.CHESS_INDEX:
                break;
        }
    }

    /**
     * A placeholder method for a message handler / event coordinator to be implemented at a later
     * time. Currently, sendNewGame clears the match history contained in GameManager, then sends
     * out a new game message using SendMessage.
     *
     * @param msg The message to transmit to the message handler.
     * @param fragmentIndex The index of the fragment that will handle the message.
     */
    public void sendNewGame(final String msg, final int fragmentIndex, final FragmentActivity context) {
        mInstructions.clear();
        setCurrentFragment(fragmentIndex, context, msg);
    }

    /**
     * Sets and loads the fragment indicated by the fragmentIndex parameter into the fragment
     * container as provided by the context parameter.
     *
     * @param fragmentIndex indicates the requested fragment
     * @param context provides access to the fragment's parent view.
     * @return true if the fragment index suggested is within our rights to access, false otherwise.
     */
    public boolean setCurrentFragment(int fragmentIndex, final FragmentActivity context, String msg) {
        // TODO: allow for further fragment selection later.
        if(fragmentIndex <= TTT_INDEX && fragmentIndex > -1) {
            if (fragmentIndex != getCurrentFragmentIndex()) {
                currentFragment = fragmentIndex;

                // Set up the new fragment in our fragment container.
                fragmentList.get(fragmentIndex).setArguments(context.getIntent().getExtras());
                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.game_pane_fragment_container, fragmentList.get(fragmentIndex))
                        .commit();

                return true;

            }
            if(msg != null) {
                sendMessage(msg, fragmentIndex);
            }
        }
        return false;
    }

    /**
     * Creates a Snackbar notification based on the parameters requested.
     *
     * @param view      Specifies the view to contain the Snackbar, as specified by Snackbar.make()
     * @param output    Specifies the string output that will be contained in the Snackbar.
     * @param color     Specifies a color of the Snackbar, as generated by ContextCompat.getColor()
     * @param endedGame Specifies whether or not the new game action should be created.
     */
    public void generateSnackbar(View view, String output, int color, boolean endedGame) {
        Snackbar notification;
        // If the game is ended, we will generate an action for the Snackbar that starts a new game.
        if(endedGame) {
            notification = Snackbar.make(view, output, Snackbar.LENGTH_INDEFINITE);
            notification.setAction("Play Again!", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Initiate a new TicTacToe game.
                    if(getCurrentFragmentIndex() == TTT_INDEX) {
                        String msg = (((TTTFragment) getFragment(TTT_INDEX)).mTurn ? "X" : "O")
                                + "\n" + "New Game";
                        sendMessage(msg, TTT_INDEX);
                    }
                }
            });
        // If the game hasn't ended, we do not need to make an action for the snackbar.
        } else {
            notification = Snackbar.make(view, output, Snackbar.LENGTH_SHORT);
        }
        // If the color is specified, we can set it to be a non-default color.
        if(color != -1) {
            notification.getView().setBackgroundColor(color);
        }
        notification.show();
    }

    // Protected instance methods

    // Private instance methods.

    // Private classes


}
