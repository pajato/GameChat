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

package com.pajato.android.gamechat.chat.fragment;

import android.support.annotation.NonNull;
import android.view.View;

import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.chat;

/**
 * Provide a fragment to deal with no account or a signed out account.
 *
 * @author Paul Michael Reilly
 */
public class ChatShowSignedOutFragment extends BaseChatFragment {

    // Public instance methods.

    /** Handle a group profile change by trying again to start a better fragment. */
    @Subscribe public void onChatListChange(@NonNull final ChatListChangeEvent event) {
        // On the first chat list change event, dismiss the sign in progress spinner.  In any case
        // attempt to present another fragment based on the chat list change.
        if (ProgressManager.instance.isShowing()) {
            ProgressManager.instance.hide();
        }
        DispatchManager.instance.startNextFragment(this.getActivity(), chat);
    }

    /** Handle the setup for the groups panel. */
    @Override public void onStart() {
        // Provide an account loading indicator for a brief period before showing the fragment.
        // This will likely be enough time to load the account and message data.
        super.onStart();
        ToolbarManager.instance.init(this);
        FabManager.chat.setVisibility(this, View.GONE);
    }
}
