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

package com.pajato.android.gamechat.chat;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.chat.ChatFragment.CHAT_HOME_FAM_KEY;

/**
 * Provide a fragment to deal with no account or a signed out account.
 *
 * @author Paul Michael Reilly
 */
public class ShowSignedOutFragment extends BaseChatFragment {

    // Public instance methods.

    /** A placeholder event handler to pacify the event manager. */
    @Subscribe public void onClick(final ClickEvent event) {}

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_chat_no_account;}

    /** Handle the setup for the groups panel. */
    @Override public void onInitialize() {
        // Provide a loading indicator, enable the options menu, layout the fragment, set up the ad
        // view and the listeners for backend data changes.
        super.onInitialize();
        FabManager.game.init(this);
    }

    /** Reset the FAM to use the game home menu. */
    @Override public void onResume() {
        // TODO: Change the FAB button menu to show "Sign In", "Add Account" or "Switch Account".
        super.onResume();
        FabManager.chat.setMenu(this, CHAT_HOME_FAM_KEY);
    }

}
