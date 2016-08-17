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

package com.pajato.android.gamechat.game;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;

/**
 * A Fragment that contains and controls the current game being played.
 *
 * @author Bryan Scott
 */
public class GameFragment extends BaseFragment {

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
        String msg = getTurn() + "\n" + getString(R.string.new_game);
        switch(item.getItemId()) {
            // If the toolbar chat icon is clicked, on smartphone devices we can change panes.
            case R.id.toolbar_chat_icon:
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if(viewPager != null) {
                    viewPager.setCurrentItem(PaneManager.ROOMS_INDEX);
                }
                break;
            // Otherwise, we can initiate a new game based on which game was chosen.
            case R.id.options_menu_new_game:
                GameManager.instance.sendNewGame(GameManager.INIT_INDEX, getActivity());
                break;
            case R.id.options_menu_new_ttt:
                if(GameManager.instance.getCurrentFragmentIndex() == GameManager.TTT_LOCAL_INDEX) {
                    GameManager.instance.sendNewGame(GameManager.TTT_LOCAL_INDEX, getActivity(), msg);
                } else if (GameManager.instance.getCurrentFragmentIndex() == GameManager.TTT_ONLINE_INDEX) {
                    GameManager.instance.sendNewGame(GameManager.TTT_ONLINE_INDEX, getActivity(), msg);
                } else {
                    GameManager.instance.sendNewGame(GameManager.SETTINGS_INDEX, getActivity(),
                            getString(R.string.new_game_ttt));
                }
                    break;
            case R.id.options_menu_new_checkers:
                GameManager.instance.sendNewGame(GameManager.CHECKERS_INDEX, getActivity(), msg);
                break;
            case R.id.options_menu_new_chess:
                GameManager.instance.sendNewGame(GameManager.CHESS_INDEX, getActivity(), msg);
                break;
        }
        return super.onOptionsItemSelected(item);
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
            // These two cases should never be called in an impactful way.
            case GameManager.INIT_INDEX:
                return null;
            case GameManager.SETTINGS_INDEX:
                return null;
            case GameManager.TTT_LOCAL_INDEX:
                return ((LocalTTTFragment) GameManager.instance
                        .getFragment(GameManager.TTT_LOCAL_INDEX))
                        .mTurn ? getString(R.string.xValue) : getString(R.string.oValue);
            case GameManager.TTT_ONLINE_INDEX:
                return ((TTTFragment) GameManager.instance
                        .getFragment(GameManager.TTT_ONLINE_INDEX))
                        .mTurn ? getString(R.string.xValue) : getString(R.string.oValue);
            case GameManager.CHECKERS_INDEX:
                return ((CheckersFragment) GameManager.instance
                        .getFragment(GameManager.CHECKERS_INDEX))
                        .mTurn ? "Blue" : "Yellow";
            case GameManager.CHESS_INDEX:
                return ((ChessFragment) GameManager.instance
                        .getFragment(GameManager.CHESS_INDEX))
                        .mTurn ? "Blue" : "Purple";
        }
    }

}
