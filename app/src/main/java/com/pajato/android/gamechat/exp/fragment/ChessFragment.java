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
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.exp.ExpHelper;
import com.pajato.android.gamechat.exp.chess.Chess;
import com.pajato.android.gamechat.exp.chess.ChessBoard;
import com.pajato.android.gamechat.exp.model.Player;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static com.pajato.android.gamechat.R.color.colorAccent;
import static com.pajato.android.gamechat.R.color.colorPrimary;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.invite;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.common.model.JoinState.JoinType.exp;

/**
 * A simple Chess game for use in GameChat.
 *
 * @author Bryan Scott
 * @author Paul Michael Reilly
 */
public class ChessFragment extends BaseExperienceFragment {

    // Public class constants.

    /**
     * The lookup key for the FAB chess menu.
     */
    public static final String CHESS_FAM_KEY = "ChessFamKey";

    /** Logcat TAG. */
    @SuppressWarnings("unused")
    private static final String TAG = ChessFragment.class.getSimpleName();

    // Public instance methods.

    /** Handle a button click event by delegating the event to the base class. */
    @Subscribe public void onClick(final ClickEvent event) {
        processClickEvent(event.view, this.type);
    }

    /** Handle a FAM or Snackbar Chess click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Delegate the event to the base class.
        processTagClickEvent(event, this.type);
    }

    /** Handle an experience posting event to see if this is a chess experience. */
    @Subscribe public void onExperienceChange(final ExperienceChangeEvent event) {
        // Delegate the event handling to the base class.
        processExperienceChange(event);
    }

    /** Handle a menu item selection. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        // Delegate to BaseExperienceFragment (super class)
        processMenuItemEvent(event);
    }

    /** Deal with the fragment's lifecycle by marking the join inactive. */
    @Override public void onPause() {
        super.onPause();
        if (mExperience != null)
            clearJoinState(mExperience.getGroupKey(), mExperience.getRoomKey(), exp);
    }

    /** Update the UI for the chess experience while running in the foreground. */
    @Override public void onResume() {
        // Determine if there is an experience ready to be enjoyed.  If not, then chill out.  When
        // an experience is posted by the app event manager, the game will be shown.  Otherwise mark
        // the room has having been joined (active join state) and show the game.
        super.onResume();
        if (mExperience == null)
            return;
        setJoinState(mExperience.getGroupKey(), mExperience.getRoomKey(), exp);
        resumeExperience();
    }

    @Override public void onStart() {
        // Setup the FAM, add a new game item to the overflow menu, and obtain the board.
        super.onStart();
        mDispatcher.expType = null;
        FabManager.game.setMenu(CHESS_FAM_KEY, getChessMenu());
        ToolbarManager.instance.init(this, helpAndFeedback, settings, chat, invite);
        mBoard.init(this, mTileClickHandler);

        // Color the Player Icons.
        ImageView playerOneIcon = (ImageView) mLayout.findViewById(R.id.player_1_icon);
        playerOneIcon.setColorFilter(ContextCompat.getColor(getContext(), colorPrimary), SRC_ATOP);
        ImageView playerTwoIcon = (ImageView) mLayout.findViewById(R.id.player_2_icon);
        playerTwoIcon.setColorFilter(ContextCompat.getColor(getContext(), colorAccent), SRC_ATOP);
    }

    // Protected instance methods.

    /** Return a default, partially populated, Chess experience. */
    @Override protected void createExperience(final Context context, final List<Account> playerAccounts) {
        // Setup the default key, players, creation timestamp and name.
        String key = getExperienceKey();
        List<Player> players = getDefaultPlayers(context, playerAccounts);
        long tStamp = new Date().getTime();
        String name = createTwoPlayerName(players, tStamp);

        // Set up the default board, group (Me Group) and room (Me Room) keys, the owner id and
        // create the object on the database.
        ChessBoard board = new ChessBoard();
        board.init();
        String groupKey = AccountManager.instance.getMeGroupKey();
        String roomKey = AccountManager.instance.getMeRoomKey();
        String id = getOwnerId();
        // TODO: DEFINE LEVEL INT ENUM VALUES - this is passing "0" for now
        Chess model = new Chess(board, key, id, 0, name, tStamp, groupKey, roomKey, players);
        mExperience = model;
        if (groupKey != null && roomKey != null)
            ExperienceManager.instance.createExperience(model);
        else
            ExpHelper.reportError(this, R.string.ErrorCheckersCreation, groupKey, roomKey);
    }

    // Private instance methods.

    /** Return the FAM menu (empty) - the FAB operates as a button here. */
    private List<MenuEntry> getChessMenu() {
        return new ArrayList<>();
    }
}
