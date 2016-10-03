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
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.MessageListChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

/**
 * Provide a fragment to handle the display of the groups available to the current user.  This is
 * the top level view in the chat hierarchy.  It shows all the joined groups and allows for drilling
 * into rooms and chats within those rooms.
 *
 * @author Paul Michael Reilly
 */
public class ShowRoomListFragment extends BaseChatFragment {

    // Public instance methods.

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_chat_rooms;}

    /** Provide a general button click handler to post the click for general consumption. */
    public void onClick(final View view) {
        AppEventManager.instance.post(new ClickEvent(view));
    }

    /** Deal with the options menu creation by making the search item visible. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Turn on both the back and search buttons.
        setItemState(menu, R.id.search, true);
    }

    /** Handle the setup for the groups panel. */
    @Override public void onInitialize() {
        // Inflate the layout for this fragment and initialize by setting the titles, declaring the
        // use of the options menu, setting up the ad view and initializing the rooms handling.
        super.onInitialize();
        setTitles(mItem.groupKey, null);
        mItemListType = DatabaseListManager.ChatListType.room;
        initAdView(mLayout);
        initList(mLayout, DatabaseListManager.instance.getList(mItemListType, mItem), false);
        FabManager.chat.init(this);
    }

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onMessageListChange(final MessageListChangeEvent event) {
        // Log the event and update the list saving the result for a retry later.
        String format = "onMessageListChange (showRoomList) with event {%s}";
        logEvent(String.format(Locale.US, format, event));
        mUpdateOnResume = !updateAdapterList();
    }

    /** Deal with the fragment's activity's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Turn on the FAB and force a recycler view  update.
        setTitles(mItem.groupKey, null);
        FabManager.chat.setState(this, View.VISIBLE);
        mUpdateOnResume = true;
        super.onResume();
    }

}
