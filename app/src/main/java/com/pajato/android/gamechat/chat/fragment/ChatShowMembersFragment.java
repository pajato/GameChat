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
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * Provide a fragment to display the members in a group.
 */

public class ChatShowMembersFragment extends BaseChatFragment {

    // Public class constants.

    /** The lookup key for the FAB game home menu. */
    public static final String CHAT_MEMBERS_FAM_KEY = "chatMembersFamKey";

    /** Process a given button click event looking for the chat FAB. */
    @Subscribe
    public void onClick(final ClickEvent event) {
        // Delegate the processing to the super class.
        processClickEvent(event.view, "showMembers");
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
                InvitationManager.instance.extendGroupInvitation(getActivity(), mItem.groupKey);
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

    /** Set up the toolbar */
    @Override public void onStart() {
        super.onStart();
        String title =
                String.format(getActivity().getString(R.string.MembersToolbarTitle), mItem.name);
        ToolbarManager.instance.init(this, title, null, helpAndFeedback, settings);
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
