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
import android.widget.Toast;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.ProfileGroupDeleteEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.createChatGroup;
import static com.pajato.android.gamechat.common.FragmentType.joinRoom;
import static com.pajato.android.gamechat.common.FragmentType.protectedUsers;
import static com.pajato.android.gamechat.common.FragmentType.selectGroupsRooms;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.game;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.search;

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

    /** Process a given button click event looking for the chat FAB. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Delegate the processing to the super class.
        processClickEvent(event.view, "showGroups");
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
                DispatchManager.instance.chainFragment(getActivity(), createChatGroup);
                break;
            case R.string.JoinRoomsMenuTitle:
                DispatchManager.instance.chainFragment(getActivity(), joinRoom);
                break;
            case R.string.InviteFriendMessage:
                DispatchManager.instance.chainFragment(getActivity(), selectGroupsRooms);
                break;
            case R.string.ManageRestrictedUserTitle:
                if (AccountManager.instance.isRestricted()) {
                    String protectedWarning = getString(R.string.CannotManageProtectedUser);
                    Toast.makeText(getActivity(), protectedWarning, Toast.LENGTH_SHORT).show();
                    break;
                }
                DispatchManager.instance.chainFragment(getActivity(), protectedUsers);
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

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onChatListChange(final ChatListChangeEvent event) {
        // Determine if this fragment cares about chat list changes.  If so, update the list
        // content.
        String format = "onChatListChange with event {%s}";
        logEvent(String.format(Locale.US, format, event));
        if (mActive)
            updateAdapterList();
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

    /** Initialize ... */
    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this, getTitleResId(), game, search);
        FabManager.chat.setMenu(CHAT_GROUP_FAM_KEY, getGroupMenu());
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getGroupMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.JoinRoomsMenuTitle, R.drawable.ic_checkers_black_24dp));
        if (!AccountManager.instance.isRestricted()) {
            menu.add(getTintEntry(R.string.CreateGroupMenuTitle,
                    R.drawable.ic_group_add_black_24dp));
            menu.add(getTintEntry(R.string.ManageRestrictedUserTitle,
                    R.drawable.ic_verified_user_black_24dp));
        }
        menu.add(getTintEntry(R.string.InviteFriendMessage, R.drawable.ic_share_black_24dp));
        return menu;
    }

    /** Return the toolbar title resource id based on the presence or absence of groups. */
    private int getTitleResId() {
        // Show the app title if there is no current account (impossible), the standard groups
        // toolbar title if there are joined groups, otherwise show the me room name (account
        // display name.)
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null)
            return R.string.app_name;
        if (account.joinMap.size() > 0)
            return R.string.ChatGroupsToolbarTitle;
        return R.string.GroupMeToolbarTitle;
    }
}
