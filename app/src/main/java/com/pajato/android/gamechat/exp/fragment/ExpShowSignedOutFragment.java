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

import android.support.annotation.NonNull;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.common.FragmentKind.exp;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment.GAME_HOME_FAM_KEY;

public class ExpShowSignedOutFragment extends BaseExperienceFragment {

    @Subscribe public void onClick(final ClickEvent event) {
        // todo add some code here.
        logEvent("onClick (showSignedOut)");
    }

    /** Handle a group profile change by trying again to start a better fragment. */
    @Subscribe public void onExperienceChange(@NonNull final ExperienceChangeEvent event) {
        // An experience event has occurred.  Ensure that we are in the right fragment.
        DispatchManager.instance.startNextFragment(this.getActivity(), exp);
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

    /** Initialize the fragment by setting up the FAB/FAM. */
    @Override public void onStart() {
        // Set up the FAB.
        super.onStart();
        FabManager.game.init(this);
        int titleResId = R.string.SignedOutTitleText;
        ToolbarManager.instance.init(this, titleResId, helpAndFeedback, chat, settings);
    }
}
