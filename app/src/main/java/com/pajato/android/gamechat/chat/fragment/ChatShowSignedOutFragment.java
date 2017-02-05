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

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.common.FragmentKind.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.game;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * Provide a fragment to deal with no account or a signed out account.
 *
 * @author Paul Michael Reilly
 */
public class ChatShowSignedOutFragment extends BaseChatFragment {

    // Public instance methods.

    /** Handle a group profile change by trying again to start a better fragment. */
    @Subscribe public void onChatListChange(@NonNull final ChatListChangeEvent event) {
        // On the first chat list change event, attempt to present another fragment based on the
        // chat list change.
        DispatchManager.instance.startNextFragment(this.getActivity(), chat);
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
            default:
                break;
        }
    }

    /** Handle the setup for the groups panel. */
    @Override public void onStart() {
        // Provide an account loading indicator for a brief period before showing the fragment.
        // This will likely be enough time to load the account and message data.
        super.onStart();
        int titleResId = R.string.SignedOutToolbarTitle;
        ToolbarManager.instance.init(this, titleResId, helpAndFeedback, game, settings);
        FabManager.chat.setVisibility(this, View.GONE);
    }
}
