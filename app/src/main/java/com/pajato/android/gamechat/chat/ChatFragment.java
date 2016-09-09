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

import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.main.PaneManager;

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

    /** Set the layout file, which specifies the chat FAB and the basic options menu. */
    @Override public int getLayout() {return R.layout.fragment_chat;}

    /** Handle a authentication change event by dealing with the fragment to display. */
    @Subscribe public void onAccountStateChange(final AccountStateChangeEvent event) {
        // Log the event and determine if there is an active account.
        logEvent("onAccountStateChange");
        if (event.account == null) {
            // There is no active account.  Show the no account fragment.
            ChatManager.instance.replaceFragment(showNoAccount, this.getActivity());
        } else {
            // There is an active account.  Determine if a default fragment needs to be set up and,
            // if so, show the group list, otherwise just let whatever fragment is running stay
            // running.
            ChatManager.ChatFragmentType type = ChatManager.instance.lastTypeShown;
            if (type == null) ChatManager.instance.replaceFragment(showGroupList, getActivity());
        }
    }

    /** Create the view to do essentially nothing. Things will happen in the onStart() method. */
    @Override public void onInitialize() {
        // Declare the use of the options menu and intialize the FAB and it's menu.
        super.onInitialize();
        FabManager.chat.init(mLayout, this.getTag());
    }

    /** Post the chat options menu on demand. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.chat_menu_base, menu);
        MenuItem item = menu.findItem(R.id.back);
        if (item != null) item.setVisible(false);
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

}
