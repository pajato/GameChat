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

import android.content.Context;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.ProfileRoomDeleteEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.groupMembersList;
import static com.pajato.android.gamechat.common.FragmentType.createChatGroup;
import static com.pajato.android.gamechat.common.FragmentType.joinRoom;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.game;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.members;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.search;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.  This is the
 * penultimate view in the chat hierarchy when there is more than one group.  It shows all the
 * joined rooms and allows for drilling into chats within those rooms.
 *
 * @author Paul Michael Reilly
 */
public class ChatShowRoomsFragment extends BaseChatFragment {

    // Public class constants.

    /** The lookup key for the FAB game home menu. */
    public static final String CHAT_ROOM_FAM_KEY = "chatRoomFamKey";

    // Public instance methods.

    /** Return null or a list to be displayed by the list adapter */
    public List<ListItem> getList() {
        return RoomManager.instance.getListItemData(mDispatcher.groupKey);
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        return GroupManager.instance.getGroupName(mDispatcher.groupKey);
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        return getString(R.string.RoomsToolbarTitle);
    }

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onChatListChange(final ChatListChangeEvent event) {
        // Determine if this fragment cares about chat list changes.  If so, do a redisplay.
        String format = "onChatListChange with event {%s}";
        logEvent(String.format(Locale.US, format, event));
        if (mActive)
            updateAdapterList();
    }

    /** Handle a button click event by delegating the event to the base class. */
    @Subscribe public void onClick(final ClickEvent event) {
        processClickEvent(event.view, this.type);
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
                DispatchManager.instance.dispatchToFragment(this, createChatGroup, null, null);
                break;
            case R.string.JoinRoomsMenuTitle:
                DispatchManager.instance.dispatchToFragment(this, joinRoom, null, null);
                break;
            case R.string.InviteFriendMessage:
                InvitationManager.instance.extendGroupInvitation(getActivity(), mDispatcher.groupKey);
                break;
            default:
                break;
        }
    }

    /** Handle a menu item selection. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        if (!this.mActive)
            return;
        // Case on the item resource id if there is one to be had.
        switch (event.item != null ? event.item.getItemId() : -1) {
            case R.string.MenuItemSearch:
                showFutureFeatureMessage(R.string.MenuItemSearch);
                break;
            case R.string.MembersMenuItem:
                DispatchManager.instance.dispatchToFragment(this, groupMembersList, null, null);
                break;
            default:
                break;
        }
    }

    @Subscribe public void onProfileRoomDelete(final ProfileRoomDeleteEvent event) {
        String format = "onProfileRoomDelete with event {%s}";
        logEvent(String.format(Locale.US, format, event));
        if (mActive)
            updateAdapterList();
    }

    /** Deal with the fragment's activity's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the group name only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu; initialize the ad view; and set up
        // the group list display.
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_add_white_24dp);
        FabManager.chat.init(this, CHAT_ROOM_FAM_KEY);
    }

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        mDispatcher = dispatcher;
    }

    /** Initialize ... */
    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this, game, search, members, helpAndFeedback, settings);
        FabManager.chat.setMenu(CHAT_ROOM_FAM_KEY, getRoomMenu());
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getRoomMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.JoinRoomsMenuTitle, R.drawable.ic_checkers_black_24dp));
        if(!AccountManager.instance.isRestricted()) {
            menu.add(getTintEntry(R.string.CreateGroupMenuTitle, R.drawable.ic_group_add_black_24dp));
        }
        menu.add(getTintEntry(R.string.InviteFriendMessage, R.drawable.ic_share_black_24dp));
        return menu;
    }

}
