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

package com.pajato.android.gamechat.exp.fragment;

import android.content.Context;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.event.AuthenticationChangeHandled;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.FragmentKind.exp;
import static com.pajato.android.gamechat.common.FragmentType.checkers;
import static com.pajato.android.gamechat.common.FragmentType.chess;
import static com.pajato.android.gamechat.common.FragmentType.setupExp;
import static com.pajato.android.gamechat.common.FragmentType.tictactoe;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * A Fragment that contains and controls the current experience shown to the User.
 *
 * @author Bryan Scott
 * @author Paul Reilly
 */
public class ExpEnvelopeFragment extends BaseExperienceFragment {

    // Public constants.

    /** The lookup key for the FAB game home menu. */
    public static final String GAME_HOME_FAM_KEY = "gameHomeFamKey";

    // Default constructor.

    /** Build an instance setting the fragment type. */
    public ExpEnvelopeFragment() {
        type = FragmentType.expEnvelope;
    }

    // Public instance methods.

    /** Satisfy base class */
    public List<ListItem> getList() {
        return null;
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        // For the envelope, the toolbar subtitle will be handled by the various fragments
        return null;
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        // For the envelope, the toolbar title will be handled by the various fragments
        return null;
    }

    /** There has been a handled authentication change event.  Deal with the fragment to display. */
    @Subscribe public void onAuthenticationChange(final AuthenticationChangeHandled event) {
        // Simply start the next logical fragment.
        DispatchManager.instance.dispatchToFragment(this, exp);
    }

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        mDispatcher = dispatcher;
    }

    /** Initialize the game fragment envelope. */
    @Override public void onStart() {
        // Inflate the layout, and initialize the various managers.
        super.onStart();
        FabManager.game.setTag(this.getTag());
        FabManager.game.setMenu(GAME_HOME_FAM_KEY, getHomeMenu());
        ToolbarManager.instance.init(this, helpAndFeedback, chat, settings);
    }

    /** Dispatch to a more suitable fragment. */
    @Override public void onResume() {
        // The experience manager will load a fragment to view into this envelope fragment.
        super.onResume();
        DispatchManager.instance.dispatchToFragment(this, exp);
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getHomeMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getEntry(R.string.SetupNewExp, R.drawable.ic_settings_black_24dp, setupExp));
        menu.add(getEntry(R.string.PlayTicTacToe, R.mipmap.ic_tictactoe_red, tictactoe));
        menu.add(getEntry(R.string.PlayCheckers, R.mipmap.ic_checkers, checkers));
        menu.add(getEntry(R.string.PlayChess, R.mipmap.ic_chess, chess));
        return menu;
    }

}
