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

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.chat;

/**
 * Provide a fragment to deal with no account or a signed out account.
 *
 * @author Paul Michael Reilly
 */
public class ChatShowSignedOutFragment extends BaseChatFragment {

    // Public constants.

    /** The sign in floating action menu (FAM) key. */
    public static final String SIGN_IN_FAM_KEY = "signInFamKey";

    // Public instance methods.

    /** Handle a FAM item click. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Ensure that the event has a menu entry payload.  Abort if not.
        Object payload = event.view.getTag();
        if (payload == null || !(payload instanceof MenuEntry)) return;

        // The event represents a menu entry.  Close the FAM and handle a click on the switch
        // account menu entry.
        FabManager.chat.dismissMenu(this);
        MenuEntry entry = (MenuEntry) payload;
        switch (entry.titleResId) {
            case R.string.SwitchAccountMenuTitle:
                showFutureFeatureMessage(R.string.SwitchAccountDesc);
                break;
            default:
                // Ignore any other items.
                break;
        }
    }

    /** Handle a sign in click event by delegating it to the FAB/FAM. */
    @Subscribe public void onClick(final ClickEvent event) {
        switch (event.view.getId()) {
            case R.id.chatSignIn:
                // Simulate a click on the chat FAB.
                FabManager.chat.toggle(this);
                break;
            default:
                break;
        }
    }

    /** Handle a group profile change by trying again to start a better fragment. */
    @Subscribe public void onChatListChange(@NonNull final ChatListChangeEvent event) {
        // On the first chat list change event, dismiss the sign in progress spinner.  In any case
        // attempt to present another fragment based on the chat list change.
        if (ProgressManager.instance.isShowing()) {
            ProgressManager.instance.hide();
        }
        DispatchManager.instance.startNextFragment(this.getActivity(), chat);
    }

    /** Handle the setup for the groups panel. */
    @Override public void onStart() {
        // Provide an account loading indicator for a brief period before showing the fragment.
        // This will likely be enough time to load the account and message data.
        super.onStart();
        FabManager.chat.init(this);
        FabManager.chat.setMenu(SIGN_IN_FAM_KEY, getSignInMenu());
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getSignInMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.SwitchAccountMenuTitle, R.drawable.ic_user_refresh));
        menu.add(getTintEntry(R.string.SignInLastAccountMenuTitle, R.drawable.vd_login_2));
        return menu;
    }
}
