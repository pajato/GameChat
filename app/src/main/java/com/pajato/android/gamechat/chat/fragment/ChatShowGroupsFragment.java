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
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.ProfileGroupDeleteEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.joinRoom;
import static com.pajato.android.gamechat.common.FragmentType.protectedUsers;
import static com.pajato.android.gamechat.common.FragmentType.selectGroupsRooms;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.game;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.search;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * Provide a fragment to handle the display of the groups available to the current user.  This is
 * the top level view in the chat hierarchy.  It shows all the joined groups and allows for drilling
 * into rooms and chats within those rooms.
 *
 * @author Paul Michael Reilly
 */
public class ChatShowGroupsFragment extends BaseChatFragment {

    // Public class constants.

    /** The lookup key for the FAB game home menu. */
    public static final String CHAT_GROUP_FAM_KEY = "chatGroupFamKey";

    // Public instance methods.

    /** Return null or a list to be displayed by the list adapter */
    public List<ListItem> getList() {
        return GroupManager.instance.getListItemData();
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        return null;
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        // Show the app title if there is no current account (impossible), the standard groups
        // toolbar title if there are joined groups, otherwise show the me room name (account
        // display name.)
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null)
            return getString(R.string.app_name);
        if (account.joinMap.size() > 0)
            return getString(R.string.ChatGroupsToolbarTitle);
        return getString(R.string.GroupMeToolbarTitle);
    }

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onChatListChange(final ChatListChangeEvent event) {
        // Determine if this fragment cares about chat list changes.  If so, update the list
        // content.
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
            case R.string.JoinRoomsMenuTitle:
                DispatchManager.instance.dispatchToFragment(this, joinRoom);
                break;
            case R.string.InviteFriendMessage:
                DispatchManager.instance.dispatchToFragment(this, selectGroupsRooms, type);
                break;
            case R.string.ManageRestrictedUserTitle:
                DispatchManager.instance.dispatchToFragment(this, protectedUsers, type);
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
            default:
                break;
        }
    }

    @Subscribe public void onProfileGroupDelete(final ProfileGroupDeleteEvent event) {
        String format = "onProfileGroupDelete with event {%s}";
        logEvent(String.format(Locale.US, format, event));
        if (mActive)
            updateAdapterList();
    }

    /** Deal with the fragment's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Use the super class to set up the list of groups and establish the FAB and it's menu.
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_add_white_24dp);
        FabManager.chat.init(this, CHAT_GROUP_FAM_KEY);
        FabManager.chat.setVisibility(this, View.VISIBLE);
    }

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        String meGroupKey = AccountManager.instance.getMeGroupKey();
        dispatcher.roomKey = meGroupKey != null && meGroupKey.equals(dispatcher.groupKey)
                ? AccountManager.instance.getMeRoomKey() : dispatcher.roomKey;
        mDispatcher = dispatcher;
    }

    /** Initialize ... */
    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this, game, search, helpAndFeedback, settings);
        FabManager.chat.setMenu(CHAT_GROUP_FAM_KEY, getGroupMenu());
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getGroupMenu() {
        // Let everyone join rooms, standard Users can manage protected Users and invitations can
        // be extended if at least one group is available with rooms to join.
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.JoinRoomsMenuTitle, R.drawable.ic_checkers_black_24dp));
        if (!AccountManager.instance.isRestricted()) {
            menu.add(getTintEntry(R.string.ManageRestrictedUserTitle,
                    R.drawable.ic_verified_user_black_24dp));
        }
        Account account = AccountManager.instance.getCurrentAccount();
        if (account != null && account.joinMap.size() > 0)
            menu.add(getTintEntry(R.string.InviteFriendMessage, R.drawable.ic_share_black_24dp));
        return menu;
    }
}
