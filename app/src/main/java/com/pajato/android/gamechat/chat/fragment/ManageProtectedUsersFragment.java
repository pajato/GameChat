/*
 * Copyright (C) 2017 Pajato Technologies LLC.
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
package com.pajato.android.gamechat.chat.fragment;

import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ProtectedUserChangeEvent;
import com.pajato.android.gamechat.event.ProtectedUserDeleteEvent;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * Provide a fragment to manage the protected users associated with the current account.
 */

public class ManageProtectedUsersFragment extends BaseChatFragment {

    // Public instance methods.

    /** Handle a button click event by delegating the event to the base class. */
    @Subscribe public void onClick(final ClickEvent event) {
        processClickEvent(event.view, this.type);
    }

    /** Handle protected user deleted events by updating the adapter */
    @Subscribe public void onProtectedUserDeleted(ProtectedUserDeleteEvent event) {
        if (!mActive)
            return;
        updateAdapterList();
    }

    /** Handle protected user changed events by updating the adapter */
    @Subscribe public void onProtectedUserChange(ProtectedUserChangeEvent event) {
        if (!mActive)
            return;
        updateAdapterList();
    }


    /** Deal with the fragment's lifecycle by managing the progress bar and the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the app title only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu.
        super.onResume();
        FabManager.chat.setVisibility(this, View.GONE);
    }

    /** Set up toolbar and FAM */
    @Override public void onStart() {
        // Establish the create type, the list type, setup the toolbar and turn off the access
        // control.
        super.onStart();
        int titleResId = R.string.ProtectedUsersTitle;
        ToolbarManager.instance.init(this, titleResId, helpAndFeedback, settings);
    }
}