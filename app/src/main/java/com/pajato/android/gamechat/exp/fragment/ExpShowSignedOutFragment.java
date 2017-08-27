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
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment.GAME_HOME_FAM_KEY;

public class ExpShowSignedOutFragment extends BaseExperienceFragment {

    /** Satisfy base class */
    public List<ListItem> getList() {
        return null;
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        return null;
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        return getString(R.string.SignedOutTitleText);
    }

    @Subscribe public void onClick(final ClickEvent event) {
        processClickEvent(event.view, FragmentType.expSignedOut);
        logEvent("onClick (showSignedOut)");
    }

    /** Deal with the fragment's activity's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the group name only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home experience menu; and display a list of groups
        // with experiences showing the rooms and highlighting new experiences, much like messages
        // in the chat group display.
        super.onResume();
        FabManager.game.setImage(R.drawable.ic_add_white_24dp);
        FabManager.game.init(this, GAME_HOME_FAM_KEY);
    }

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        mDispatcher = dispatcher;
    }

    /** Initialize the fragment by setting up the FAB/FAM. */
    @Override public void onStart() {
        // Set up the FAB.
        super.onStart();
        FabManager.game.init(this);
        ToolbarManager.instance.init(this, helpAndFeedback, chat, settings);
    }
}
