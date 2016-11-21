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

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import static com.pajato.android.gamechat.chat.ChatFragment.CHAT_HOME_FAM_KEY;

/**
 * Provide a fragment to handle the display of the groups available to the current user.  This is
 * the top level view in the chat hierarchy.  It shows all the joined groups and allows for drilling
 * into rooms and chats within those rooms.
 *
 * @author Paul Michael Reilly
 */
public class ShowGroupListFragment extends BaseChatFragment {

    // Public instance methods.

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_chat_groups;}

    /** Deal with the options menu by making the search button visible. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Turn on the search option.
        setItemState(menu, R.id.search, true);
    }

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onChatListChange(final ChatListChangeEvent event) {
        // Log the event and update the list saving the result for a retry later.
        logEvent(String.format(Locale.US, "onMessageListChange with event {%s}", event));
        mUpdateOnResume = !updateAdapterList();
    }

    /** Deal with the fragment's lifecycle by managing the progress bar and the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the app title only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu; initialize the ad view; and set up
        // the group list display.
        setTitles(null, null);
        FabManager.chat.init(this, View.VISIBLE, CHAT_HOME_FAM_KEY);
        initAdView(mLayout);
        mItemListType = DatabaseListManager.ChatListType.group;
        initList(mLayout, DatabaseListManager.instance.getList(mItemListType, mItem), false);
        mUpdateOnResume = true;
        super.onResume();
    }

}
