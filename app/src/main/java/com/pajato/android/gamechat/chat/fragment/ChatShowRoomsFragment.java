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

import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.createGroup;
import static com.pajato.android.gamechat.common.FragmentType.createRoom;
import static com.pajato.android.gamechat.common.FragmentType.joinRoom;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.search;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.  This is the
 * penultimate view in the chat hierarchy when there is more than one group.  It shows all the
 * joined rooms and allows for drilling into chats within those rooms.
 *
 * @author Paul Michael Reilly
 */
public class ChatShowRoomsFragment extends BaseChatFragment {

    // Public class constants.

    /** The lookup key for the FAB game home memu. */
    public static final String CHAT_ROOM_FAM_KEY = "chatRoomFamKey";

    // Public instance methods.

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onChatListChange(final ChatListChangeEvent event) {
        // Determine if this fragment cares about chat list changes.  If so, do a redisplay.
        String format = "onChatListChange with event {%s}";
        logEvent(String.format(Locale.US, format, "no list", event));
        if (mActive)
            redisplay();
    }

    /** Process a menu click event ... */
    @Subscribe public void onClick(final TagClickEvent event) {
        Object payload = event.view.getTag();
        if (payload == null || !(payload instanceof MenuEntry)) return;

        // The event represents a menu entry.  Close the FAM and case on the title id.
        FabManager.chat.dismissMenu(this);
        MenuEntry entry = (MenuEntry) payload;
        switch (entry.titleResId) {
            case R.string.CreateGroupMenuTitle:
                DispatchManager.instance.chainFragment(getActivity(), createGroup, null);
                break;
            case R.string.CreateRoomMenuTitle:
                DispatchManager.instance.chainFragment(getActivity(), createRoom, mItem);
                break;
            case R.string.JoinRoomsMenuTitle:
                DispatchManager.instance.chainFragment(getActivity(), joinRoom, null);
                break;
            case R.string.InviteFriendFromChat:
                InvitationManager.instance.extendAppInvitation(getActivity(), mItem.groupKey);
            default:
                // ...
                break;
        }
    }

    /** Initialize ... */
    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this, search);
        FabManager.chat.setMenu(CHAT_ROOM_FAM_KEY, getRoomMenu());
    }

    /** Deal with the fragment's activity's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the group name only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu; initialize the ad view; and set up
        // the group list display.
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_add_white_24dp);
        FabManager.chat.init(this, CHAT_ROOM_FAM_KEY);
        FabManager.chat.setVisibility(this, View.VISIBLE);
    }

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getRoomMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.JoinRoomsMenuTitle, R.drawable.ic_casino_black_24dp));
        menu.add(getTintEntry(R.string.CreateRoomMenuTitle, R.drawable.ic_casino_black_24dp));
        if(AccountManager.instance.getCurrentAccount().chaperone == null) {
            menu.add(getTintEntry(R.string.CreateGroupMenuTitle, R.drawable.ic_group_add_black_24dp));
        }
        menu.add(getNoTintEntry(R.string.InviteFriendFromChat, R.drawable.ic_email_black_24dp));
        return menu;
    }

}
