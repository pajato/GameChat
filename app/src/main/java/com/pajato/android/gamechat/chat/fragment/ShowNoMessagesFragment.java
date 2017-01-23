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

import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.event.MessageChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.chat;
import static com.pajato.android.gamechat.event.BaseChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.BaseChangeEvent.NEW;

public class ShowNoMessagesFragment extends BaseChatFragment {

    // Public instance methods.

    /** Handle a message list change event by starting the next fragment, as necessary. */
    @Subscribe public void onMessageListChange(MessageChangeEvent event) {
        switch (event.changeType) {
            case CHANGED:
            case NEW:
                DispatchManager.instance.startNextFragment(getActivity(), chat);
                break;
            default:
                break;
        }
    }
}
