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

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showGroupList;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showNoAccount;

/**
 * Provide a fragment class that decides which alternative chat fragment to show to the User.
 * Indecision will result in a default "flummoxed" message being displayed.
 *
 * @author Paul Michael Reilly (based on GameFragment written by Bryan Scott)
 */
public class ChatFragment extends BaseFragment {

    // Public instance methods.

    /** Handle a authentication event. */
    @Subscribe public void onAccountStateChange(final AccountStateChangeEvent event) {
        // Register an active rooms change handler on the account, if there is an account,
        // preferring a cached handler, if one is available.
        if (event.account == null) {
            // There is no active account.  Show the no account fragment.
            ChatManager.instance.replaceFragment(showNoAccount, this.getActivity());
        } else {
            // There is an active account.  Show a list of messages by room in the groups for which
            // the account is a member.
            ChatManager.instance.replaceFragment(showGroupList, this.getActivity());
        }
    }

    /** Create the view to do essentially nothing. Things will happen in the onStart() method. */
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Inflate the layout, and initialize the game manager.
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    /** Post the chat options menu on demand. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.chat_menu_base, menu);
    }

    /** Handle an options menu choice. */
    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_game_icon:
                // Show the game panel.
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if(viewPager != null) {
                    viewPager.setCurrentItem(PaneManager.GAME_INDEX);
                }
                break;
            case R.id.search:
                // TODO: Handle a search in the groups panel by fast scrolling to chat.
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /** Kick off fragment processing by having the chat manager decide what to do. */
    @Override public void onStart() {
        // Now that the fragment is visible, initialize the account manager and show a progress
        // spinner during the initialization process.
        super.onStart();
        View layout = getView();
        if (layout != null) {
            ProgressManager.instance.show(this.getActivity());
            FabManager.chat.init(layout);
            EventBusManager.instance.register(this);
            ChatListManager.instance.init();
            AccountManager.instance.init();
        }
    }

}
