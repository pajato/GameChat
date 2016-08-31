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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

/**
 * Provide a fragment to deal with no account or a signed out account.
 *
 * TODO: Change the FAB button menu to show "Sign In", "Add Account" or "Switch Account".
 *
 * @author Paul Michael Reilly
 */
public class ShowNoAccountFragment extends BaseFragment {

    // Public instance methods.

    /** Process a given button click event looking for one on the chat fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Determine if this event is for the chat fab button.
        int value = event.getView() != null ? event.getView().getId() : 0;
        switch (value) {
            // TODO: add cases for sign in, add account and switch account?
        default:
            // Ignore everything else.
            break;
        }
    }

    /** Handle an account state change event by showing the no sign in message. */
    @Subscribe public void onAccountStateChange(final AccountStateChangeEvent event) {
        // TODO: handle an account becoming available by switching to the show group list fragment.
    }

    /** Handle the setup for the groups panel. */
    @Override public View onCreateView(final LayoutInflater inflater,
                                       final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Provide a loading indicator, enable the options menu, layout the fragment, set up the ad
        // view and the listeners for backend data changes.
        //ProgressManager.instance.show(this.getContext());
        setHasOptionsMenu(true);
        EventBusManager.instance.register(this);
        return inflater.inflate(R.layout.fragment_chat_no_account, container, false);
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
