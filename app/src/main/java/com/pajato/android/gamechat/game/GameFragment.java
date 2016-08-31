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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.FabManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

/**
 * A Fragment that contains and controls the current game being played.
 *
 * @author Bryan Scott
 */
public class GameFragment extends BaseFragment {

    public GameFragment() {
        // Required empty public constructor
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout, and initialize the various managers.
        View layout = inflater.inflate(R.layout.fragment_game, container, false);
        setHasOptionsMenu(true);
        EventBusManager.instance.register(this);
        GameManager.instance.init(getActivity());
        FabManager.game.init(layout);
        return layout;
    }

    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.game_menu, menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() ==  R.id.toolbar_chat_icon) {
            // If the toolbar chat icon is clicked, on smartphone devices we can change panes.
            ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
            if (viewPager != null) {
                viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
            }
        } else if (item.getItemId() == R.id.options_menu_new_game) {
            GameManager.instance.sendNewGame(GameManager.INIT_INDEX, getActivity());
        }
        return super.onOptionsItemSelected(item);
    }

    /** Process a given button click event looking for one on the rooms fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Grab the View ID and the floating action button and dimmer views.
        int viewId = event.getView() != null ? event.getView().getId() : 0;
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.games_fab);
        View backgroundDimmer =getActivity().findViewById(R.id.games_background_dimmer);

        // When a button is clicked, send a new game and reset the fab menu and background dimmer.
        if(viewId == R.id.init_ttt || viewId == R.id.init_ttt_button) {
            GameManager.instance.sendNewGame(GameManager.SETTINGS_INDEX, getActivity(),
                    getString(R.string.new_game_ttt));
            FabManager.game.dismissMenu(fab);
            backgroundDimmer.setVisibility(View.GONE);
        // Do it for checkers.
        } else if (viewId == R.id.init_checkers || viewId == R.id.init_checkers_button) {
            GameManager.instance.sendNewGame(GameManager.SETTINGS_INDEX, getActivity(),
                    getString(R.string.new_game_checkers));
            FabManager.game.dismissMenu(fab);
            backgroundDimmer.setVisibility(View.GONE);
        // Do it for chess.
        } else if (viewId == R.id.init_chess || viewId == R.id.init_chess_button) {
            GameManager.instance.sendNewGame(GameManager.SETTINGS_INDEX, getActivity(),
                    getString(R.string.new_game_chess));
            FabManager.game.dismissMenu(fab);
            backgroundDimmer.setVisibility(View.GONE);
        // And do it for the rooms option buttons.
        } else if (viewId == R.id.init_rooms || viewId == R.id.init_rooms_button) {
            GameManager.instance.sendNewGame(GameManager.INIT_INDEX, getActivity());
            FabManager.game.dismissMenu(fab);
            backgroundDimmer.setVisibility(View.GONE);
        // If the click is on the fab, we have to handle if it's open or closed.
        } else if (viewId == R.id.games_fab) {
            FabManager.game.toggle(fab);
            if(backgroundDimmer.getVisibility() == View.VISIBLE) {
                backgroundDimmer.setVisibility(View.GONE);
            } else {
                backgroundDimmer.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Sends a message alerting the event handling system that there was a tile clicked, and
     * swaps the mTurn to the opposite player.
     *
     * @param view the tile clicked
     */
    public void tileOnClick(final View view) {
        String msg = GameManager.instance.getTurn() + "\n" + view.getTag().toString();
        GameManager.instance.sendMessage(msg, GameManager.instance.getCurrentFragmentIndex());
    }

}
