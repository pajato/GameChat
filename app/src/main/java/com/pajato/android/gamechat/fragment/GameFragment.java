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
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.game.GameManager;
import com.pajato.android.gamechat.main.PaneManager;

/**
 * A Fragment that contains and controls the current game being played.
 *
 * @author Bryan Scott
 */
public class GameFragment extends BaseFragment{

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout, and initialize the game manager.
        View layout = inflater.inflate(R.layout.fragment_game, container, false);
        setHasOptionsMenu(true);
        GameManager.instance.init(getActivity());
        return layout;
    }

    // Public Instance Methods
    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.game_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Determine if the new game being initiated is different than what is currently loaded.
        switch(item.getItemId()) {
            case R.id.toolbar_chat_icon:
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if(viewPager != null) {
                    viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                }
                break;
            case R.id.adv_new_game: onNewGame(GameManager.SETTINGS_INDEX);
                break;
            case R.id.ttt_new_game: onNewGame(GameManager.TTT_INDEX);
                break;
            case R.id.checkers_new_game: onNewGame(GameManager.CHECKERS_INDEX);
                break;
            case R.id.chess_new_game: onNewGame(GameManager.CHESS_INDEX);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends a message alerting the event handling system that the new game button was clicked.
     *
     * @param fragmentIndicator the ID of the new game button in the floating action button menu.
     */
    private void onNewGame(final int fragmentIndicator) {
        // Create the message and send a new game.
        String msg = getTurn() + "\n" + getString(R.string.new_game);
        GameManager.instance.sendNewGame(msg, fragmentIndicator, getActivity());
    }

    /**
     * Sends a message alerting the event handling system that there was a tile clicked, and
     * swaps the mTurn to the opposite player.
     *
     * @param view the tile clicked
     */
    public void tileOnClick(final View view) {
        String msg = getTurn() + "\n" + view.getTag().toString();
        GameManager.instance.sendMessage(msg, GameManager.instance.getCurrentFragmentIndex());
    }

    //Private Instance Methods

    /**
     * Gets the current mTurn and returns a string reflecting the player's
     * name who is currently playing.
     *
     * @return player 1 or player 2, depending on the mTurn.
     */
    private String getTurn() {
        switch(GameManager.instance.getCurrentFragmentIndex()) {
            default:
            case GameManager.SETTINGS_INDEX:
                // Do nothing. We do not have turns in this fragment, so it should never be called.
                return null;
            case GameManager.TTT_INDEX:
                return ((TTTFragment) GameManager.instance.getFragment(GameManager.TTT_INDEX))
                        .mTurn ? getString(R.string.xValue) : getString(R.string.oValue);
            case GameManager.CHECKERS_INDEX:
                return null;
            case GameManager.CHESS_INDEX:
                return null;
        }
    }

}
