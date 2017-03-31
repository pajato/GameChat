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
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MemberManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.FragmentType.groupMembersList;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * Provide a fragment to display the members in a group.
 */

public class ChatShowMembersFragment extends BaseChatFragment {

    // Public class constants.

    /** The lookup key for the FAB game home menu. */
    public static final String CHAT_MEMBERS_FAM_KEY = "chatMembersFamKey";

    // Public instance methods.

    /** Return null or a list to be displayed by the list adapter */
    public List<ListItem> getList() {
        if (this.type == groupMembersList)
            return MemberManager.instance.getGroupMemberListItemData(mDispatcher.groupKey);
        else
            return MemberManager.instance.getRoomMemberListItemData(mDispatcher.groupKey,
                mDispatcher.roomKey);
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        if (this.type == groupMembersList)
            return null;
        return GroupManager.instance.getGroupName(mDispatcher.groupKey);
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        if (this.type == groupMembersList) {
            String groupName = GroupManager.instance.getGroupName(mDispatcher.groupKey);
            return String.format(getString(R.string.MembersToolbarTitle), groupName);
        }
        String roomName = RoomManager.instance.getRoomName(mDispatcher.roomKey);
        return String.format(getString(R.string.MembersToolbarTitle), roomName);
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
            case R.string.InviteFriendMessage:
                InvitationManager.instance.extendGroupInvitation(getActivity(),
                        mDispatcher.groupKey);
            default:
                break;
        }
    }

    /** Ensure that the FAB is not visible. Disable the 'finish' button. */
    @Override public void onResume() {
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_add_white_24dp);
        FabManager.chat.init(this, CHAT_MEMBERS_FAM_KEY);
        FabManager.chat.setVisibility(this, View.VISIBLE);
    }

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        // The experiences in a room require both the group and room keys.  Determine if the
        // group is the me group and give it special handling.
        String groupKey = dispatcher.groupKey;
        String meGroupKey = AccountManager.instance.getMeGroupKey();
        dispatcher.roomKey = meGroupKey != null && meGroupKey.equals(groupKey)
                ? AccountManager.instance.getMeRoomKey() : dispatcher.roomKey;
        mDispatcher = dispatcher;
    }

    /** Set up the toolbar */
    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this, helpAndFeedback, settings);
        FabManager.chat.setMenu(CHAT_MEMBERS_FAM_KEY, getFabMenu());
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getFabMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.InviteFriendMessage, R.drawable.ic_share_black_24dp));
        return menu;
    }
}
