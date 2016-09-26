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
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.adapter.MenuItemEntry;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.game.Game.checkers;
import static com.pajato.android.gamechat.game.Game.chess;
import static com.pajato.android.gamechat.game.Game.ttt;
import static com.pajato.android.gamechat.game.GameManager.NO_GAMES_INDEX;
import static com.pajato.android.gamechat.game.GameManager.SETTINGS_INDEX;

/**
 * A Fragment that contains and controls the current game being played.
 *
 * @author Bryan Scott
 */
public class GameFragment extends BaseGameFragment {

    // Public constants.

    /** The lookup key for the FAB game home memu. */
    public static final String GAME_HOME_FAM_KEY = "gameHomeFamKey";

    /** Process a given button click event looking for one on the game fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Grab the View ID and the floating action button and dimmer views.
        View view = event.view;
        String title = null;
        Game game = null;
        switch (view.getId()) {
            case R.id.IconTicTacToe:
            case R.mipmap.ic_tictactoe_red:
                // When a button is clicked, send a new game and reset the fab menu and background
                // dimmer.
                title = getString(R.string.new_game_ttt);
                game = ttt;
                break;
            case R.id.IconCheckers:
            case R.mipmap.ic_checkers:
                // Do it for checkers.
                title = getString(R.string.new_game_checkers);
                game = checkers;
                break;
            case R.id.IconChess:
            case R.mipmap.ic_chess:
                // Do it for chess.
                title = getString(R.string.new_game_chess);
                game = chess;
                break;
            case R.drawable.ic_casino_black_24dp:
                // And do it for the rooms option buttons.
                showFutureFeatureMessage(R.string.FutureSelectRooms);
                FabManager.game.dismissMenu(this);
                break;
            case R.id.gameFab:
                // If the click is on the fab, we have to handle if it's open or closed.
                FabManager.game.toggle(this);
                break;
            default:
                break;
        }

        if (title != null && game != null) {
            GameManager.instance.sendNewGame(SETTINGS_INDEX, getActivity(), title, game);
            FabManager.game.dismissMenu(this);
        }
    }

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_game;}

    /** Handle a tile click event by sending a message to the current tic-tac-toe fragment. */
    @Subscribe public void onClick(final TagClickEvent event) {
        int index = GameManager.instance.getCurrent();
        if (index == GameManager.TTT_LOCAL_INDEX || index == GameManager.TTT_ONLINE_INDEX) {
            String msg = GameManager.instance.getTurn() + "\n" + event.view.getTag().toString();
            GameManager.instance.sendMessage(msg, index);
        }
    }

    /** Handle the options menu by inflating it. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.game_menu, menu);
    }

    /** Intialize the game fragment envelope. */
    @Override public void onInitialize() {
        // Inflate the layout, and initialize the various managers.
        super.onInitialize();
        mGame = null;
        GameManager.instance.init(getActivity());
        FabManager.game.setTag(this.getTag());
        FabManager.game.setMenu(GAME_HOME_FAM_KEY, getHomeMenu());
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
                GameManager.instance.sendNewGame(NO_GAMES_INDEX, getActivity());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /** Satisfy the base game fragment contract with a nop message handler. */
    @Override public void messageHandler(final String message) {}

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getHomeMenu() {
        List<MenuEntry> menu = new ArrayList<>();
        final int tttIconResId = R.mipmap.ic_tictactoe_red;
        menu.add(new MenuEntry(new MenuItemEntry(R.string.PlayTicTacToe, tttIconResId)));
        menu.add(new MenuEntry(new MenuItemEntry(R.string.PlayCheckers, R.mipmap.ic_checkers)));
        menu.add(new MenuEntry(new MenuItemEntry(R.string.PlayChess, R.mipmap.ic_chess)));
        final int gotoRoomsIconResId = R.drawable.ic_casino_black_24dp;
        menu.add(new MenuEntry(new MenuItemEntry(R.string.GoToRooms, gotoRoomsIconResId)));
        return menu;
    }
}
