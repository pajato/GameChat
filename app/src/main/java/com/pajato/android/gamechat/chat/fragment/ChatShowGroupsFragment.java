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

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.main.PaneManager;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.createGroup;
import static com.pajato.android.gamechat.common.FragmentType.joinRoom;
import static com.pajato.android.gamechat.common.FragmentType.selectChatGroupsRooms;
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
            case R.string.JoinRoomsMenuTitle:
                DispatchManager.instance.chainFragment(getActivity(), joinRoom, null);
                break;
            case R.string.InviteFriendFromChat:
                DispatchManager.instance.chainFragment(getActivity(), selectChatGroupsRooms, null);
                break;
            case R.string.CreateRestrictedUserTitle:
                if (AccountManager.instance.getCurrentAccount().chaperone != null) {
                    String protectedWarning = "Protected Users cannot make other Protected Users.";
                    Toast.makeText(getActivity(), protectedWarning, Toast.LENGTH_SHORT).show();
                    break;
                }
                AccountManager.instance.mChaperone = AccountManager.instance.getCurrentAccountId();
                FirebaseAuth.getInstance().signOut();
                AccountManager.instance.signIn(getContext());
                break;
            default:
                // ...
                break;
        }
    }

    /** Handle a menu item selection. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        if (!this.mActive)
            return;
        // Case on the item resource id if there is one to be had.
        switch (event.item != null ? event.item.getItemId() : -1) {
            case R.string.SwitchToExp:
                // If the toolbar game icon is clicked, on smart phone devices we can change panes.
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if (viewPager != null) viewPager.setCurrentItem(PaneManager.GAME_INDEX);
                break;
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
        logEvent(String.format(Locale.US, format, "no list", event));
        if (mActive)
            updateAdapterList();
    }

    /** Initialize ... */
    @Override public void onStart() {
        super.onStart();
        if (ProgressManager.instance.isShowing())
            ProgressManager.instance.hide();
        ToolbarManager.instance.init(this, game, search);
        FabManager.chat.setMenu(CHAT_GROUP_FAM_KEY, getGroupMenu());
    }

    /** Deal with the fragment's lifecycle by managing the progress bar and the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the app title only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu; initialize the ad view; and set up
        // the group list display.
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_add_white_24dp);
        FabManager.chat.init(this, CHAT_GROUP_FAM_KEY);
        FabManager.chat.setVisibility(this, View.VISIBLE);
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getGroupMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.JoinRoomsMenuTitle, R.drawable.ic_casino_black_24dp));
        if (!AccountManager.instance.isRestricted()) {
            menu.add(getTintEntry(R.string.CreateGroupMenuTitle,
                    R.drawable.ic_group_add_black_24dp));
            menu.add(getTintEntry(R.string.CreateRestrictedUserTitle,
                    R.drawable.ic_person_add_black_24px));
        }
        menu.add(getTintEntry(R.string.InviteFriendFromChat, R.drawable.ic_share_black_24dp));
        return menu;
    }

}
