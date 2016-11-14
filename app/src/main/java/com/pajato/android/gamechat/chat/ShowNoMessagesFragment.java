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
import com.pajato.android.gamechat.event.MessageChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.chat.ChatFragment.CHAT_HOME_FAM_KEY;
import static com.pajato.android.gamechat.event.BaseChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.BaseChangeEvent.NEW;

public class ShowNoMessagesFragment extends BaseChatFragment {

    // Public instance methods.

    /** Establish the layout file to indicate that no experiences are available. */
    @Override public int getLayout() {return R.layout.fragment_chat_no_messages;}

    /** Handle a message list change event by starting the next fragment, as necessary. */
    @Subscribe public void onMessageListChange(MessageChangeEvent event) {
        switch (event.changeType) {
            case CHANGED:
            case NEW:
                ChatManager.instance.startNextFragment(getActivity());
                break;
            default:
                break;
        }
    }

    /** Reset the FAM to use the chat home menu. */
    @Override public void onResume() {
        super.onResume();
        FabManager.chat.setMenu(this, CHAT_HOME_FAM_KEY);
    }
}
