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
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.DBUtils;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.chat.ChatFragmentType.createGroup;
import static com.pajato.android.gamechat.chat.ChatFragmentType.createRoom;
import static com.pajato.android.gamechat.chat.ChatFragmentType.joinRoom;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.  This is the
 * penultimate view in the chat hierarchy when there is more than one group.  It shows all the
 * joined rooms and allows for drilling into chats within those rooms.
 *
 * @author Paul Michael Reilly
 */
public class ShowRoomListFragment extends BaseChatFragment {

    // Public class constants.

    /** The lookup key for the FAB game home memu. */
    public static final String CHAT_ROOM_FAM_KEY = "chatRoomFamKey";

    // Public instance methods.

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_chat_list;}

    /** Process a menu click event ... */
    @Subscribe public void onClick(final TagClickEvent event) {
        Object payload = event.view.getTag();
        if (payload == null || !(payload instanceof MenuEntry)) return;

        // The event represents a menu entry.  Close the FAM and case on the title id.
        FabManager.chat.dismissMenu(this);
        MenuEntry entry = (MenuEntry) payload;
        switch (entry.titleResId) {
            case R.string.CreateGroupMenuTitle:
                ChatManager.instance.chainFragment(createGroup, getActivity(), mItem);
                break;
            case R.string.CreateRoomMenuTitle:
                ChatManager.instance.chainFragment(createRoom, getActivity(), mItem);
                break;
            case R.string.JoinRoomsMenuTitle:
                ChatManager.instance.chainFragment(joinRoom, getActivity(), mItem);
                break;
            default:
                // ...
                break;
        }
    }

    /** Deal with the options menu creation by making the search item visible. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Turn on both the back and search buttons.
        setItemState(menu, R.id.search, true);
    }

    /** Initialize ... */
    @Override public void onInitialize() {
        super.onInitialize();
        mItemListType = DBUtils.ChatListType.room;
        initToolbar();
        FabManager.chat.setMenu(CHAT_ROOM_FAM_KEY, getRoomMenu());
    }

    /** Deal with the fragment's activity's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the group name only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu; initialize the ad view; and set up
        // the group list display.
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_add_white_24dp);
        FabManager.chat.init(this);
        FabManager.chat.setVisibility(this, View.VISIBLE);
        FabManager.chat.setMenu(this, CHAT_ROOM_FAM_KEY);
    }

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getRoomMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.JoinRoomsMenuTitle, R.drawable.vd_casino_black_24px));
        menu.add(getTintEntry(R.string.CreateRoomMenuTitle, R.drawable.vd_casino_black_24px));
        menu.add(getTintEntry(R.string.CreateGroupMenuTitle, R.drawable.ic_group_add_black_24dp));
        return menu;
    }

}
