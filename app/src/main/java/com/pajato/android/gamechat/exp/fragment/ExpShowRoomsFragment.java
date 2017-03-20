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

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.common.FragmentType.experienceList;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.invite;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.search;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.expRoom;
import static com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment.GAME_HOME_FAM_KEY;

public class ExpShowRoomsFragment extends BaseExperienceFragment {

    /** Handle a button click event by delegating the event to the base class. */
    @Subscribe public void onClick(final ClickEvent event) {
        processClickEvent(event.view, this.type);
    }

    /** Handle a FAM or Snackbar click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Delegate the event to the base class.
        processTagClickEvent(event, this.type);
    }

    @Override public void onResume() {
        super.onResume();
        FabManager.game.setImage(R.drawable.ic_add_white_24dp);
        FabManager.game.init(this, GAME_HOME_FAM_KEY);
    }

    /** Initialize the fragment by setting in the FAB. */
    @Override public void onStart() {
        // Ensure that this is not a pass-through to a particular experience fragment.  If not, then
        // initialize the FAM.
        super.onStart();
        if (mDispatcher.expType == null) {
            FabManager.game.init(this);
            ToolbarManager.instance.init(this, mItem, helpAndFeedback, chat, search, invite, settings);
            return;
        }

        // Handle a pass-through by handing off to the experience list fragment.
        mDispatcher.type = experienceList;
        DispatchManager.instance.chainFragment(getActivity(), mDispatcher);
        mItem = new ListItem(expRoom, mDispatcher.groupKey, null, null, 0, null);
    }
}
