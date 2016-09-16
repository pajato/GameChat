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

import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.chat.FabManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.game.GameManager.INIT_INDEX;
import static com.pajato.android.gamechat.game.GameManager.SETTINGS_INDEX;

/**
 * A Fragment that contains and controls the current game being played.
 *
 * @author Bryan Scott
 */
public class GameFragment extends BaseGameFragment {

    /** Process a given button click event looking for one on the game fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Grab the View ID and the floating action button and dimmer views.
        int viewId = event.getView() != null ? event.getView().getId() : 0;
        switch (viewId) {
            case R.id.init_ttt:
            case R.id.init_ttt_button:
                // When a button is clicked, send a new game and reset the fab menu and background
                // dimmer.
                String title = getString(R.string.new_game_ttt);
                GameManager.instance.sendNewGame(SETTINGS_INDEX, getActivity(), title);
                FabManager.game.dismissMenu(this);
                break;
            case R.id.init_checkers:
            case R.id.init_checkers_button:
                // Do it for checkers.
                title = getString(R.string.new_game_checkers);
                GameManager.instance.sendNewGame(SETTINGS_INDEX, getActivity(), title);
                FabManager.game.dismissMenu(this);
                break;
            case R.id.init_chess:
            case R.id.init_chess_button:
                // Do it for chess.
                title = getString(R.string.new_game_chess);
                GameManager.instance.sendNewGame(SETTINGS_INDEX, getActivity(), title);
                FabManager.game.dismissMenu(this);
                break;
            case R.id.init_rooms:
            case R.id.init_rooms_button:
                // And do it for the rooms option buttons.
                GameManager.instance.sendNewGame(INIT_INDEX, getActivity());
                FabManager.game.dismissMenu(this);
                break;
            case R.id.games_fab:
                // If the click is on the fab, we have to handle if it's open or closed.
                FabManager.game.toggle(this);
                break;
            default:
                break;
        }
    }

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_game;}

    /** Handle the options menu by inflating it. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.game_menu, menu);
    }

    /** Intialize the game fragment envelope. */
    @Override public void onInitialize() {
        // Inflate the layout, and initialize the various managers.
        super.onInitialize();
        GameManager.instance.init(getActivity());
        FabManager.game.init(mLayout, this.getTag());
    }

    /** Handle a menu item selection. */
    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        // Case on the item.
        switch (item.getItemId()) {
            case R.id.toolbar_chat_icon:
                // If the toolbar chat icon is clicked, on smartphone devices we can change panes.
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if (viewPager != null) viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                break;
            case R.id.options_menu_new_game:
                // ...
                GameManager.instance.sendNewGame(INIT_INDEX, getActivity());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
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
